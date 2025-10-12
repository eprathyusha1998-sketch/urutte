package com.urutte.service;

import com.urutte.dto.CommentDto;
import com.urutte.model.Comment;
import com.urutte.model.CommentLike;
import com.urutte.model.Post;
import com.urutte.model.User;
import com.urutte.repository.CommentLikeRepository;
import com.urutte.repository.CommentRepository;
import com.urutte.repository.PostRepository;
import com.urutte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    
    public CommentDto createComment(Long postId, String content, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = new Comment(content, post, user);
        Comment savedComment = commentRepository.save(comment);
        
        // Update post comments count (only main comments)
        long commentsCount = commentRepository.countByPostAndParentCommentIsNull(post);
        post.setCommentsCount((int) commentsCount);
        postRepository.save(post);
        
        return convertToDto(savedComment, userId);
    }
    
    public CommentDto createReply(Long parentCommentId, String content, String userId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment reply = new Comment(content, parentComment.getPost(), user, parentComment);
        Comment savedReply = commentRepository.save(reply);
        
        // Update parent comment replies count
        long repliesCount = commentRepository.countByParentComment(parentComment);
        parentComment.setRepliesCount((int) repliesCount);
        commentRepository.save(parentComment);
        
        // Note: We don't update post comments count for replies since we only count main comments
        
        return convertToDto(savedReply, userId);
    }
    
    public Page<CommentDto> getComments(Long postId, int page, int size, String currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post, pageable);
        
        return comments.map(comment -> {
            CommentDto dto = convertToDto(comment, currentUserId);
            // Load replies for each comment
            List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
            dto.setReplies(replies.stream()
                    .map(reply -> convertToDto(reply, currentUserId))
                    .collect(Collectors.toList()));
            return dto;
        });
    }
    
    public List<CommentDto> getReplies(Long parentCommentId, String currentUserId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);
        return replies.stream()
                .map(reply -> convertToDto(reply, currentUserId))
                .collect(Collectors.toList());
    }
    
    private CommentDto convertToDto(Comment comment, String currentUserId) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        dto.setUserPicture(comment.getUser().getPicture());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setLikesCount(comment.getLikesCount());
        dto.setRepliesCount(comment.getRepliesCount());
        
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        
        // Check if current user liked this comment
        boolean isLiked = commentLikeRepository.existsByUserIdAndCommentId(currentUserId, comment.getId());
        dto.setLiked(isLiked);
        
        return dto;
    }
    
    public boolean deleteComment(Long commentId, String userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            // Check if user owns the comment
            if (comment.getUser().getId().equals(userId)) {
                // Delete all replies first (recursively)
                deleteCommentReplies(comment);
                
                // Delete all comment likes
                commentLikeRepository.deleteByComment(comment);
                
                // If this is a reply, update parent comment's replies count
                if (comment.getParentComment() != null) {
                    Comment parentComment = comment.getParentComment();
                    long repliesCount = commentRepository.countByParentComment(parentComment);
                    parentComment.setRepliesCount((int) Math.max(0, repliesCount - 1));
                    commentRepository.save(parentComment);
                } else {
                    // If this is a main comment, update post comments count
                    Post post = comment.getPost();
                    long commentsCount = commentRepository.countByPostAndParentCommentIsNull(post);
                    post.setCommentsCount((int) Math.max(0, commentsCount - 1));
                    postRepository.save(post);
                }
                
                // Delete the comment
                commentRepository.delete(comment);
                return true;
            }
        }
        return false;
    }
    
    private void deleteCommentReplies(Comment parentComment) {
        // Get all replies for this comment
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);
        
        for (Comment reply : replies) {
            // Recursively delete nested replies
            deleteCommentReplies(reply);
            
            // Delete all likes for this reply
            commentLikeRepository.deleteByComment(reply);
            
            // Delete the reply
            commentRepository.delete(reply);
        }
    }
    
    public CommentDto likeComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if already liked
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);
        
        if (existingLike.isPresent()) {
            // Unlike: remove the like
            commentLikeRepository.delete(existingLike.get());
            comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
        } else {
            // Like: add the like
            CommentLike like = new CommentLike(user, comment);
            commentLikeRepository.save(like);
            comment.setLikesCount(comment.getLikesCount() + 1);
        }
        
        commentRepository.save(comment);
        return convertToDto(comment, userId);
    }
}

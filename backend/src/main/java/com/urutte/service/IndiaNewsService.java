package com.urutte.service;

import com.urutte.service.ContentGenerationService.NewsItem;
import com.urutte.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndiaNewsService {
    
    private static final Logger logger = LoggerFactory.getLogger(IndiaNewsService.class);
    
    /**
     * Generate India-specific news content
     */
    public List<NewsItem> generateIndiaNews(Topic topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        
        try {
            String[] keywords = topic.getKeywordsArray();
            String topicName = topic.getName().toLowerCase();
            
            // Generate content based on specific India topics
            if (topicName.contains("india") || topicName.contains("indian")) {
                newsItems.addAll(generateIndiaGeneralNews(keywords));
            }
            
            if (topicName.contains("tamil") || topicName.contains("tamil nadu")) {
                newsItems.addAll(generateTamilNaduNews(keywords));
            }
            
            if (topicName.contains("cricket")) {
                newsItems.addAll(generateCricketNews(keywords));
            }
            
            if (topicName.contains("bollywood")) {
                newsItems.addAll(generateBollywoodNews(keywords));
            }
            
            if (topicName.contains("politics")) {
                newsItems.addAll(generateIndiaPoliticsNews(keywords));
            }
            
            logger.info("Generated {} India-specific news items for topic: {}", newsItems.size(), topic.getName());
            
        } catch (Exception e) {
            logger.error("Error generating India news for topic: {}", topic.getName(), e);
        }
        
        return newsItems;
    }
    
    /**
     * Generate general India news
     */
    private List<NewsItem> generateIndiaGeneralNews(String[] keywords) {
        List<NewsItem> items = new ArrayList<>();
        
        String[] indiaNewsTemplates = {
            "India's GDP growth rate shows positive signs in Q3 2024",
            "New Delhi announces major infrastructure development projects",
            "Indian Space Research Organisation (ISRO) launches new satellite mission",
            "India's digital payment system UPI expands to more countries",
            "Indian government introduces new education policy reforms",
            "Mumbai becomes hub for fintech startups in Asia",
            "India's renewable energy capacity reaches new milestone",
            "Indian Railways introduces new high-speed train services",
            "India's startup ecosystem attracts record foreign investment",
            "New Delhi hosts international climate change summit"
        };
        
        for (String template : indiaNewsTemplates) {
            NewsItem item = new NewsItem();
            item.setTitle(template);
            item.setContent(generateIndiaContent(template));
            item.setUrl("https://news.india.com/" + System.currentTimeMillis());
            item.setScore(80 + (int)(Math.random() * 20));
            item.setPublishedAt(new Date());
            item.setSource("India News Generator");
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Generate Tamil Nadu specific news
     */
    private List<NewsItem> generateTamilNaduNews(String[] keywords) {
        List<NewsItem> items = new ArrayList<>();
        
        String[] tamilNaduNewsTemplates = {
            "Chennai Metro expansion project gets government approval",
            "Tamil Nadu announces new industrial policy for 2024",
            "Madurai temple festival attracts thousands of devotees",
            "Tamil Nadu government launches new healthcare initiative",
            "Coimbatore becomes center for textile manufacturing",
            "Tamil Nadu's IT sector shows strong growth in Q4",
            "Chennai hosts international film festival",
            "Tamil Nadu introduces new agricultural subsidy scheme",
            "Madras High Court delivers landmark judgment",
            "Tamil Nadu's renewable energy projects gain momentum"
        };
        
        for (String template : tamilNaduNewsTemplates) {
            NewsItem item = new NewsItem();
            item.setTitle(template);
            item.setContent(generateTamilNaduContent(template));
            item.setUrl("https://tamil.news/" + System.currentTimeMillis());
            item.setScore(85 + (int)(Math.random() * 15));
            item.setPublishedAt(new Date());
            item.setSource("Tamil Nadu News Generator");
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Generate cricket news
     */
    private List<NewsItem> generateCricketNews(String[] keywords) {
        List<NewsItem> items = new ArrayList<>();
        
        String[] cricketNewsTemplates = {
            "India vs Australia Test series begins in Melbourne",
            "Virat Kohli scores century in IPL 2024 match",
            "BCCI announces new selection committee",
            "Indian cricket team wins T20 World Cup",
            "MS Dhoni announces retirement from international cricket",
            "IPL 2024 auction sees record-breaking bids",
            "Indian women's cricket team reaches World Cup final",
            "Rohit Sharma breaks Sachin's record for most sixes",
            "India's U-19 cricket team wins World Cup",
            "Chennai Super Kings wins IPL 2024 championship"
        };
        
        for (String template : cricketNewsTemplates) {
            NewsItem item = new NewsItem();
            item.setTitle(template);
            item.setContent(generateCricketContent(template));
            item.setUrl("https://cricket.india.com/" + System.currentTimeMillis());
            item.setScore(90 + (int)(Math.random() * 10));
            item.setPublishedAt(new Date());
            item.setSource("Cricket News Generator");
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Generate Bollywood news
     */
    private List<NewsItem> generateBollywoodNews(String[] keywords) {
        List<NewsItem> items = new ArrayList<>();
        
        String[] bollywoodNewsTemplates = {
            "Shah Rukh Khan's new movie breaks box office records",
            "Deepika Padukone announces new project with Netflix",
            "Aamir Khan's latest film receives critical acclaim",
            "Priyanka Chopra wins international award",
            "Ranbir Kapoor and Alia Bhatt's wedding photos go viral",
            "Salman Khan's Eid release dominates box office",
            "Kangana Ranaut's new film addresses social issues",
            "Akshay Kumar's patriotic film releases on Independence Day",
            "Kareena Kapoor returns to Bollywood after maternity break",
            "Hrithik Roshan's action film becomes blockbuster hit"
        };
        
        for (String template : bollywoodNewsTemplates) {
            NewsItem item = new NewsItem();
            item.setTitle(template);
            item.setContent(generateBollywoodContent(template));
            item.setUrl("https://bollywood.india.com/" + System.currentTimeMillis());
            item.setScore(85 + (int)(Math.random() * 15));
            item.setPublishedAt(new Date());
            item.setSource("Bollywood News Generator");
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Generate India politics news
     */
    private List<NewsItem> generateIndiaPoliticsNews(String[] keywords) {
        List<NewsItem> items = new ArrayList<>();
        
        String[] politicsNewsTemplates = {
            "Prime Minister Modi announces new economic reforms",
            "Parliament passes landmark education bill",
            "Indian government launches digital India 2.0 initiative",
            "Election Commission announces state assembly polls",
            "Supreme Court delivers verdict on constitutional matter",
            "Indian government signs trade agreement with ASEAN",
            "Defense Minister announces new military modernization plan",
            "Finance Minister presents Union Budget 2024",
            "Indian government launches Make in India 2.0",
            "Parliamentary committee submits report on healthcare reforms"
        };
        
        for (String template : politicsNewsTemplates) {
            NewsItem item = new NewsItem();
            item.setTitle(template);
            item.setContent(generatePoliticsContent(template));
            item.setUrl("https://politics.india.com/" + System.currentTimeMillis());
            item.setScore(80 + (int)(Math.random() * 20));
            item.setPublishedAt(new Date());
            item.setSource("India Politics News Generator");
            
            items.add(item);
        }
        
        return items;
    }
    
    /**
     * Generate content for India news
     */
    private String generateIndiaContent(String title) {
        return "This development in India represents a significant milestone for the country's progress. " +
               "The initiative aligns with India's vision of becoming a global leader in innovation and development. " +
               "Experts believe this will have positive implications for India's economy and international standing. " +
               "The government's commitment to this project demonstrates India's forward-thinking approach to national development.";
    }
    
    /**
     * Generate content for Tamil Nadu news
     */
    private String generateTamilNaduContent(String title) {
        return "This announcement from Tamil Nadu government reflects the state's commitment to progress and development. " +
               "Tamil Nadu continues to lead in various sectors including IT, manufacturing, and education. " +
               "The initiative will benefit millions of people across the state and strengthen Tamil Nadu's position as a key economic hub. " +
               "This development showcases Tamil Nadu's rich cultural heritage while embracing modern technology and innovation.";
    }
    
    /**
     * Generate content for cricket news
     */
    private String generateCricketContent(String title) {
        return "This cricket development has generated excitement among millions of Indian cricket fans. " +
               "The Indian cricket team continues to perform exceptionally well on the international stage. " +
               "This achievement adds to India's rich cricketing legacy and inspires young players across the country. " +
               "Cricket remains India's most beloved sport, uniting people from all walks of life.";
    }
    
    /**
     * Generate content for Bollywood news
     */
    private String generateBollywoodContent(String title) {
        return "This Bollywood development highlights the global reach and influence of Indian cinema. " +
               "Bollywood continues to entertain audiences worldwide with its unique storytelling and cultural richness. " +
               "The Indian film industry's success reflects the talent and creativity of Indian artists and filmmakers. " +
               "This achievement showcases Bollywood's ability to connect with diverse audiences across different cultures.";
    }
    
    /**
     * Generate content for politics news
     */
    private String generatePoliticsContent(String title) {
        return "This political development reflects the Indian government's commitment to democratic governance and public welfare. " +
               "The initiative demonstrates India's strong democratic institutions and transparent decision-making process. " +
               "This policy will have far-reaching implications for India's development and international relations. " +
               "The government's approach shows India's dedication to inclusive growth and social progress.";
    }
}

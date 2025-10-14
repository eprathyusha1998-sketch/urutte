# Component Refactoring Guide

This document outlines the improved component structure and reusable UI components created for the Urutte frontend application.

## 🏗️ New Architecture

### 1. UI Components (`/src/components/ui/`)

A comprehensive set of reusable UI components that follow consistent design patterns:

#### Core Components

- **`Avatar`** - User profile pictures with fallback initials
- **`Button`** - Consistent button styling with variants and states
- **`Card`** - Flexible container component with customizable styling
- **`Modal`** - Accessible modal dialog component
- **`MediaGrid`** - Responsive media display component
- **`ContentRenderer`** - Handles hashtag and mention rendering
- **`TimeAgo`** - Formatted timestamp display

#### Component Features

- **Consistent API**: All components follow similar prop patterns
- **TypeScript Support**: Full type definitions for all props
- **Dark Mode**: Built-in dark mode support
- **Accessibility**: ARIA attributes and keyboard navigation
- **Responsive**: Mobile-first responsive design
- **Customizable**: Extensive className and styling options

### 2. Custom Hooks (`/src/hooks/`)

Reusable logic extracted into custom hooks:

#### Available Hooks

- **`useAuth`** - Authentication state management
- **`useThreadActions`** - Thread interaction logic (like, repost, delete)
- **`useClickOutside`** - Click outside detection for dropdowns/modals

#### Benefits

- **Separation of Concerns**: UI logic separated from business logic
- **Reusability**: Hooks can be used across multiple components
- **Testing**: Easier to unit test isolated logic
- **Performance**: Optimized state management and effect handling

### 3. Constants (`/src/constants/`)

Centralized configuration and constants:

- **`routes.ts`** - Application route definitions
- **`ui.ts`** - UI-related constants (sizes, variants, etc.)

### 4. Refactored Components

#### ThreadCardRefactored
- Uses new UI components (Avatar, Button, Card, etc.)
- Improved prop interface and type safety
- Better separation of concerns
- Consistent styling and behavior

#### SidebarRefactored
- Modular navigation structure
- Reusable navigation items configuration
- Improved dropdown handling with custom hook
- Better accessibility and keyboard navigation

#### PostActionsRefactored
- Simplified action handling
- Modal integration for quote reposts
- Loading states and error handling
- Consistent button styling

## 🚀 Usage Examples

### Basic Component Usage

```tsx
import { Button, Avatar, Card, Modal } from './components/ui';
import { useAuth, useThreadActions } from './hooks';

// Using UI components
<Button variant="primary" size="lg" onClick={handleClick}>
  Click me
</Button>

<Avatar 
  src={user.picture} 
  name={user.name} 
  size="md" 
  onClick={handleUserClick}
/>

<Card padding="lg" shadow="md" hover>
  <p>Card content</p>
</Card>

// Using custom hooks
const { currentUser, logout } = useAuth();
const { handleLike, isActionLoading } = useThreadActions();
```

### Refactored Component Usage

```tsx
import { ThreadCardRefactored as ThreadCard } from './components';

<ThreadCard
  thread={thread}
  currentUser={currentUser}
  onLike={handleLike}
  onRepost={handleRepost}
  onDelete={handleDelete}
  compact={false}
  showCommentButton={true}
/>
```

## 📁 File Structure

```
src/
├── components/
│   ├── ui/                    # Reusable UI components
│   │   ├── Avatar.tsx
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   ├── ContentRenderer.tsx
│   │   ├── MediaGrid.tsx
│   │   ├── Modal.tsx
│   │   ├── TimeAgo.tsx
│   │   └── index.ts
│   ├── ThreadCardRefactored.tsx
│   ├── SidebarRefactored.tsx
│   ├── PostActionsRefactored.tsx
│   └── ... (existing components)
├── hooks/                     # Custom hooks
│   ├── useAuth.ts
│   ├── useThreadActions.ts
│   ├── useClickOutside.ts
│   └── index.ts
├── constants/                 # Application constants
│   ├── routes.ts
│   ├── ui.ts
│   └── index.ts
├── pages/
│   └── FeedPageRefactored.tsx
└── ...
```

## 🎯 Benefits of Refactoring

### 1. **Reusability**
- UI components can be used across the entire application
- Consistent styling and behavior
- Reduced code duplication

### 2. **Maintainability**
- Centralized component logic
- Easier to update and modify
- Clear separation of concerns

### 3. **Type Safety**
- Full TypeScript support
- Compile-time error checking
- Better IDE support and autocomplete

### 4. **Performance**
- Optimized re-renders
- Efficient state management
- Lazy loading capabilities

### 5. **Developer Experience**
- Consistent API patterns
- Comprehensive documentation
- Easy to test and debug

## 🔄 Migration Strategy

### Phase 1: Gradual Adoption
1. Use new UI components in new features
2. Gradually replace existing components
3. Maintain backward compatibility

### Phase 2: Full Migration
1. Replace all instances of old components
2. Remove deprecated components
3. Update all imports and references

### Phase 3: Optimization
1. Performance optimization
2. Bundle size reduction
3. Advanced features and enhancements

## 🧪 Testing

The refactored components are designed to be easily testable:

```tsx
// Example test for Button component
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './components/ui';

test('Button renders with correct text and handles click', () => {
  const handleClick = jest.fn();
  render(<Button onClick={handleClick}>Click me</Button>);
  
  const button = screen.getByText('Click me');
  fireEvent.click(button);
  
  expect(handleClick).toHaveBeenCalledTimes(1);
});
```

## 📝 Best Practices

### 1. **Component Composition**
- Use composition over inheritance
- Keep components focused and single-purpose
- Leverage prop drilling for simple cases

### 2. **State Management**
- Use custom hooks for complex state logic
- Keep component state minimal
- Prefer derived state over stored state

### 3. **Styling**
- Use consistent design tokens
- Leverage Tailwind CSS classes
- Support dark mode by default

### 4. **Accessibility**
- Include ARIA attributes
- Support keyboard navigation
- Ensure proper focus management

## 🔮 Future Enhancements

### Planned Features
- **Theme System**: Advanced theming capabilities
- **Animation Library**: Smooth transitions and animations
- **Form Components**: Comprehensive form building blocks
- **Data Display**: Tables, lists, and data visualization
- **Layout Components**: Grid, flex, and layout utilities

### Performance Optimizations
- **Virtual Scrolling**: For large lists and feeds
- **Image Optimization**: Lazy loading and responsive images
- **Bundle Splitting**: Code splitting for better performance
- **Caching**: Intelligent caching strategies

This refactoring provides a solid foundation for building scalable, maintainable, and performant React applications while maintaining excellent developer experience.

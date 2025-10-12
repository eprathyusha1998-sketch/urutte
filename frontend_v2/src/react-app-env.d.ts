/// <reference types="react-scripts" />

declare namespace JSX {
  interface IntrinsicElements {
    'ion-icon': {
      name?: string;
      ios?: string;
      md?: string;
      class?: string;
      className?: string;
      style?: React.CSSProperties;
      onClick?: React.MouseEventHandler<HTMLElement>;
      [key: string]: any;
    };
  }
}

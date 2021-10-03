import { requireNativeComponent } from 'react-native';
import { CompositeComponentProps } from './WebViewTypes';

const CompositeComponent  = requireNativeComponent<CompositeComponentProps>(
	'RNTCompositeComponent',
);

export default CompositeComponent;
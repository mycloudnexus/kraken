import BasicAuthenticate from '../../basic/BasicAuthenticate';

export interface AuthenticateProps {
  children?: React.ReactNode;
}

const Authenticate = ({ children }: AuthenticateProps) => {
  return (<BasicAuthenticate children={ children } />);
}

export default Authenticate





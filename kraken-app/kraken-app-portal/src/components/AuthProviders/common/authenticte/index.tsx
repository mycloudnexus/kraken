import BasicAuthenticate from '../../basic/provider/BasicAuthenticate';

export interface AuthenticateProps {
  children?: React.ReactNode;
}

const Authenticate = ({ children }: AuthenticateProps) => {
  return (<BasicAuthenticate children={ children } />);
}

export default Authenticate





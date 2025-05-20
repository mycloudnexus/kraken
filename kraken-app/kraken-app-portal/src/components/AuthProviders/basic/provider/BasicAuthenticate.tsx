import { useNavigate } from 'react-router-dom'
import { ROUTES } from '@/utils/constants/route'
import { useEffect } from 'react';
import { Skeleton } from 'antd';
import { BasicAuthenticateProps, useBasicAuth } from './BasicAuthProvider';


const BasicAuthenticate = ({ children }: BasicAuthenticateProps) => {
  const { checkAuthenticated } = useBasicAuth();
  const navigate = useNavigate()
  useEffect(() => {
    if (!checkAuthenticated()) {
      navigate(ROUTES.LOGIN);
    }
  }, [checkAuthenticated])

  if (!checkAuthenticated()) {
    return <Skeleton />
  }
  return children
}

export default BasicAuthenticate

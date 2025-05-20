import React from 'react'
import { ENV } from '@/constants'
import { BasicAuthProvider } from '../../basic/provider/BasicAuthProvider'

interface AuthProviderProps {
  children: React.ReactElement
}

window.portalConfig = ENV;

export const AuthProvider = ({ children }: AuthProviderProps) => {
  return (<BasicAuthProvider children={ children } />);
}

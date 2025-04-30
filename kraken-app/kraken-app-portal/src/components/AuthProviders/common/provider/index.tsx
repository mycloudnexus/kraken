import React from 'react'
import { ENV } from '@/constants'
import { BasicAuthProvider } from '../../basic/BasicAuthProvider'

interface AuthProviderProps {
  children: React.ReactElement
}

window.portalConfig = ENV;

export const AuthProvider = ({ children }: AuthProviderProps) => {
  return (<BasicAuthProvider children={ children } />);
}

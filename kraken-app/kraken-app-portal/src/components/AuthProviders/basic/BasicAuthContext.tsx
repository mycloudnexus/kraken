import { createContext } from "react";
import { AuthUser } from "./types";

const err = (): any => {
};

export const initialAuthState: BasicAuthState<AuthUser> = {
  isAuthenticated: false,
  isLoading: true
};

export const initialContext = {
  ...initialAuthState,
  loginWithCredentials: err,
  getAccessToken: err,
  logout: err,
  checkAuthenticated: err,
  getCurrentAuthUser: err
};

export declare class BasicAuthUser {
  name?: string;
  given_name?: string;
  family_name?: string;
  middle_name?: string;
  nickname?: string;
  preferred_username?: string;
  profile?: string;
  picture?: string;
  website?: string;
  email?: string;
  email_verified?: boolean;
  gender?: string;
  birthdate?: string;
  zoneinfo?: string;
  locale?: string;
  phone_number?: string;
  phone_number_verified?: boolean;
  address?: string;
  updated_at?: string;
  sub?: string;
  [key: string]: any;
}

export interface BasicAuthState<TUser extends BasicAuthUser = BasicAuthUser> {
  error?: Error;
  isAuthenticated: boolean;
  isLoading: boolean;
  user?: TUser;
}

export interface BasicAuthContextInterface<TUser extends BasicAuthUser = BasicAuthUser>
  extends BasicAuthState<TUser> {
    loginWithCredentials: (values: any) => Promise<void>;
    getAccessToken: () => Promise<string>; 
    logout: () => Promise<void>;
    checkAuthenticated: () => boolean;
    getCurrentAuthUser: () => any;
}

const BasicAuthContext = createContext<BasicAuthContextInterface>(initialContext);

export default BasicAuthContext;
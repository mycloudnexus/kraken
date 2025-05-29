
import { BasicAuthState, BasicAuthUser } from "./BasicAuthContext";

export enum AuthStates {
  LOGIN_STARTED = "LOGIN_STARTED",
  LOGIN_COMPLETE = "LOGIN_COMPLETE",
  LOGOUT = "LOGOUT",
  ERROR = "ERROR",
}

type Action =
  | { type:  AuthStates.LOGIN_STARTED}
  | {
      type:  AuthStates.LOGIN_COMPLETE;
      user?: BasicAuthUser;
    }
  | { type:  AuthStates.LOGOUT }
  | { 
      type:  AuthStates.ERROR;
      error: Error 
    };

export const stateReducer = (state: BasicAuthState, action: Action)
    : BasicAuthState => {
  switch (action.type) {
    case AuthStates.LOGIN_STARTED:
      return {
        ...state,
        isLoading: true,
      };
    case AuthStates.LOGIN_COMPLETE:
      return {
        ...state,
        isAuthenticated: !!action.user,
        user: action.user,
        isLoading: false,
        error: undefined,
      };
    case AuthStates.LOGOUT:
      return {
        ...state,
        isAuthenticated: false,
        user: undefined,
      };
    case AuthStates.ERROR:
      return {
        ...state,
        isLoading: false,
        error: action.error,
      };
  }
};

declare module '*.svg' {
  import * as React from 'react'

  export const ReactComponent: React.FunctionComponent<React.SVGProps<SVGSVGElement> & { title?: string }>

  const src: string
  export default src
}

declare module '*.module.css'
declare module '*.module.scss'
declare module '*.png'

declare interface Window {
  portalConfig: Record<string, unknown> & {
    getAccessToken?: {
      (
        options: GetTokenSilentlyOptions & { detailedResponse: true }
      ): Promise<GetTokenSilentlyVerboseResponse>;
      (options?: GetTokenSilentlyOptions): Promise<string>;
      (options: GetTokenSilentlyOptions): Promise<
        GetTokenSilentlyVerboseResponse | string
      >;
    };
    checkAuthenticated?: () => boolean;
    getCurrentAuthUser?: () => any;
  }
  portalLoggedInUser: Record<string, unknown>
  portalAccessRoles: Record<string, unknown>
  portalToken: string
  io: (options?: Partial<ManagerOptions & SocketOptions>) => Socket
  portalCurrentCompany: string | null
}
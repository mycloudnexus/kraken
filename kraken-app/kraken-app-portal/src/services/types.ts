export interface ICompany {
  name: string
  id: string
  display_name: string
  connection: Connection
  metadata: MetaData
  branding: Branding
}

export interface Connection {
  name: string
  strategy: string
  display_name: string
  options: {
    signInEndpoint: string
    cert: string
    userIdAttribute: string
    signSAMLRequest: boolean
    signatureAlgorithm: string
    digestAlgorithm: string
    protocolBinding: string
    fieldsMap: FieldsMap
    subject?: Subject
  }
  id: string
  enabled_clients: string[]
  provisioning_ticket_url: string
  metadata: {
    additionalProp1: string
    additionalProp2: string
    additionalProp3: string
  }
  realms: string
}

interface MetaData {
  status: string
  strategy: string
  connectionId: string
  createdAt?: string
}

interface Branding {
  logo_url: string
  colors: Colors
}

interface Colors {
  primary: string
  page_background: string
}

export interface CreateOrganizationRequestBody {
  name: string
  display_name: string
}

export interface CreateOrganizationResponse {
  code: number
  message: string
  data: ICompany
}

export interface IOrganization {
  data: ICompany[]
  page: number
  size: number
  total: number
}

export interface RequestResponse<T> {
  code: number
  message: string
  data: T
}
export interface RequestStatusResponse<> {
  code: number
  message: string
}
export interface UpdateOrganizationRequestBody {
  request_body: { display_name?: string; status?: string }
  id: string
}

interface Samlp {
  signingCert: string
  userIdAttribute: string
  signSAMLRequest: boolean
  signatureAlgorithm?: string
  digestAlgorithm?: string
  protocolBinding: string
  fieldsMap?: FieldsMap
  signInEndpoint: string
  signOutEndpoint?: string
  debug: boolean
}

export interface AddConnectionRequestBody {
  strategy: string
  saml?: Samlp
}

export interface AddConnectionResponse {
  name: string
  strategy: string
  options: Options
  id: string
  enabled_clients: string[]
  provisioning_ticket_url: string
  realms: string[]
}

export interface Options {
  signInEndpoint: string
  signingCert: string
  debug: boolean
  signOutEndpoint?: string
  signSAMLRequest: boolean
  digestAlgorithm: string
  signatureAlgorithm: string
  fieldsMap?: FieldsMap
  expires: string
  subject: Subject
  thumbprints: string[]
  cert: string
}

export interface FieldsMap {
  email: string
}

export interface Subject {
  commonName: string
  organizationName?: string
}

interface Inviter {
  name: string
}

interface Invitee {
  email: string
}
export interface InviteUserResponse {
  inviter: Inviter
  invitee: Invitee
  client_id: string
  id: string
  connection_id: string
  ticket_id: string
  invitation_url: string
  organization_id: string
  roles: string[]
}

export interface InviteCompanyMemberRequestBody {
  userId: string
  roles: string[]
  sendEmail?: boolean
}

export interface InviteCompanyMemberResponse {
  id: string
  name: string
  email: string
  roles: string[]
  status: string
}

export interface RegisteredUsers {
  user_id: string
  name: string
  email: string
  logins_count: number
  blocked: boolean
}
export interface GetRegisteredUsersResponse {
  page: number
  size: number
  data: RegisteredUsers[]
}
export interface OrganizationInvitations {
  id: string
  inviter: Inviter
  invitee: Invitee
  connection_id: string
  send_invitation_email: boolean
  created_at: string
  expires_at: string
  ticket_id: string
  invitation_url: string
  organization_id: string
}
export interface GetInvitedUsersResponse {
  page: number
  size: number
  data: OrganizationInvitations[]
}
export interface OrganizationUsersTable {
  name: string
  id: string
  email: string
  status: string
}
export interface UpdateCustomerUserParams {
  given_name?: string
  family_name?: string
  blocked?: boolean
}
export interface CustomerOrg {
  id: string
  state: string
  userId: string
  roleIds: string[]
  roles: string[]
}
export interface CustomerMemember {
  id: string
  productId: string
  createdAt: string
  createdBy: string
  updatedAt: string
  updatedBy: string
  username: string
  name: string
  email: string
  roles: string[]
  organizationId: string
  status: string
  organization: CustomerOrg
}
export interface ResponseType<T> {
  data: {
    code: number
    message: string
    data: T
  }
}
export interface SignUpPayload {
  invitationId: string
  orgId: string
  given_name: string
  family_name: string
  password: string
}
export interface ResellerUser {
  id: string
  user_id: string
  name: string
  email: string
  blocked: boolean
}

export interface ResellerUsers {
  data: ResellerUser[]
  page: number
  size: number
  total: number
}
export interface CompanyMember {
  id: string
  name: string
  email: string
}
export interface UserEmailOption {
  label: string
  value: string
}
export interface Action {
  label: string
  isActionParamEmail?: boolean
  onClick?: (e: React.MouseEvent, id: string) => void
}

export interface Status {
  dotColor: string
  displayStatus: string
  actions: Action[]
}

export type UserStatusMap = Record<string, Status>

export interface StatsResponse {
  activeCount: number
  activeSiteCount: number
}

export interface OrderStatsResponse {
  portCount: number
  connectionCount: number
  cloudRouterCount: number
  total: number
}

export interface OrdersStatsParams {
  customerId: string
  start: string
  end: string
}
export interface CustomerPortsData {
  data: Ports[]
  total: number
  page: number
  size: number
}

export interface Ports {
  port: Port
  utilization: Utilization
}

export interface Port {
  id: string
  name: string
  status: string
  updatedAt: string
  speed: Speed
  capacity: Capacity
  dataCenterFacility: DataCenterFacility
}

export interface Capacity {
  remaining: number
  total: number
  utilised: number
}

export interface DataCenterFacility {
  name: string
  username: string
  company: Company
}

export interface Company {
  addresses: Address[]
}

export interface Address {
  city: string
  country: string
}

export interface Speed {
  name: string
  value: number
}

export interface Utilization {
  unit: string
  portId: string
  results: Result[]
}

export interface Result {
  time: number
  rxMax: number
  rxMin: number
  rxAverage: number
  txMax: number
  txMin: number
  txAverage: number
}

export interface CustomerPortsParams {
  start: number
  end: number
  resolution: string
  page: number
  size: number
}

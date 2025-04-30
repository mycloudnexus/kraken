import { DefaultLogo } from './DefaultLogo'
import { ENV } from '@/constants'
import * as styles from './index.module.scss'
export const withoutLogo = ENV.COMPANY_LOGO_URL?.includes('RUNTIME') || !ENV.COMPANY_LOGO_URL

const CompanyLogo = () => {
  return (
    <div className={styles.logoContainer} data-testid='companyLogo'>
      {withoutLogo ? <DefaultLogo /> : <img src={ENV.COMPANY_LOGO_URL} alt='logo' />}
    </div>
  )
}

export default CompanyLogo

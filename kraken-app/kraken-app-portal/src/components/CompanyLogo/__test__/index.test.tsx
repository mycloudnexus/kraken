import { render } from '@testing-library/react'

import CompanyLogo from '..'
import { ENV } from '@/constant'

describe('Icon component page', () => {
  jest.mock('@/constant', () => ({
    ENV: { COMPANY_LOGO_URL: 'https://www.m1.com/m1.png' }
  }))
  it('logo render', () => {
    const { container } = render(<CompanyLogo />)
    const imgEle = container.querySelector('img')
    expect(imgEle?.src).toEqual(ENV.COMPANY_LOGO_URL)
  })
})

describe('Icon component page', () => {
  it('logo render', () => {
    ENV.COMPANY_LOGO_URL = ''
    const { container } = render(<CompanyLogo />)
    const imgEle = container.querySelector('img')
    expect(imgEle).toBeFalsy()
  })
})

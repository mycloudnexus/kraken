import { render } from '@testing-library/react'

import { DefaultLogo } from '../DefaultLogo'

describe('Icon component page', () => {
  it('logo render', () => {
    const { container } = render(<DefaultLogo />)
    expect(container.firstElementChild?.tagName).toBe('svg')
  })
})

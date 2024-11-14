import { render } from "@/__test__/utils"
import { Alert } from "@/components/Alert"

describe('antd custom component testing', () => {
  it('should render alert component', () => {
    const { getByTestId } = render(<Alert data-testid="alert1" type="info" description="Hello world" />)
    expect(getByTestId('alert1')).toHaveTextContent('Hello world')
  })
})

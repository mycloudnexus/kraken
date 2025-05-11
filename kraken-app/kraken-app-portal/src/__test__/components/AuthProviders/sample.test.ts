import { requestToken } from '@/components/AuthProviders/basic/components/utils/request';
import axios from 'axios'

vi.mock('axios')

test('mocked axios', async () => {
  requestToken("1");

  expect(axios.get).toHaveBeenCalledWith('string')
  expect(axios.post).toBeUndefined()
})

test('can get actual axios', async () => {
  const ax = await vi.importActual<typeof axios>('axios')

  expect(vi.isMockFunction(ax.get)).toBe(false)
})

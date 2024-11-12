import { trimPath } from '@/utils/helpers/url';
import { Typography } from 'antd';

const TrimmedPath = ({ path, trimLevel, color }: { path: string, trimLevel?: number, color?: string }) => (
  path ?
    <Typography.Text
      style={color ? { color } : undefined}
      ellipsis={{ tooltip: path }}
    >
      {trimPath(path, trimLevel)}
    </Typography.Text>
    : null
)

export default TrimmedPath;
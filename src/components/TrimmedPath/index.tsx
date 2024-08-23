import { Typography } from 'antd';

const TrimmedPath = ({ path, trimLevel, color }: { path: string, trimLevel?: number, color?: string }) => (
  path ?
    <Typography.Text
      style={color ? { color } : undefined}
      ellipsis={{ tooltip: path }}
    >
      {path.split('/').slice(trimLevel ? -trimLevel : 0).join('/')}
    </Typography.Text>
    : null
)

export default TrimmedPath;
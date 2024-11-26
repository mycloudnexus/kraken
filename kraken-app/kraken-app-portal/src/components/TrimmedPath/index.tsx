import { trimPath } from '@/utils/helpers/url';
import { Typography } from 'antd';
import styles from './index.module.scss'
import { TextProps } from 'antd/es/typography/Text';
import classNames from 'classnames';

const TrimmedPath = ({ path, trimLevel, color, className, style = {}, ...props }: Readonly<{ path: string, trimLevel?: number, color?: string } & TextProps>) => (
  path ?
    <Typography.Text
      {...props}
      data-testid="apiPath"
      style={color ? { ...style, color } : style}
      ellipsis={{ tooltip: path }}
      className={classNames(styles.path, className)}
    >
      <span>{trimPath(path, trimLevel)}</span>
    </Typography.Text>
    : null
)

export default TrimmedPath;
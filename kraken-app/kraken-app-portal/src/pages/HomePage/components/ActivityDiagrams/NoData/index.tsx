import { Row, Col, Typography } from 'antd';
import PropTypes from 'prop-types';
import { SearchIcon } from '../../Icon';

const { Title, Text } = Typography;

const NoData = ({ icon: Icon = SearchIcon, title = "No data", description = "When errors occur, they will be displayed here." }) => {
  return (
    <Row
      justify="center"
      align="middle"
      style={{ height: '100%', textAlign: 'center' }}
      className="no-data-container"
    >
      <Col>
        <Icon />
        <Title style={{ color: '#595959', fontWeight: 400, fontSize: 14 }}>
          {title}
        </Title>
        <Text type="secondary">{description}</Text>
      </Col>
    </Row>
  );
};

NoData.propTypes = {
  icon: PropTypes.elementType,
  title: PropTypes.string,
  description: PropTypes.string,
};

export default NoData;

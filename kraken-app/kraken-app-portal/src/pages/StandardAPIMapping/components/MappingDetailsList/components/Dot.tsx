import styles from '../index.module.scss';

const Dot = ({ vertical }: { vertical?: boolean }) => (
  <div className={vertical ? styles.dottedLine : styles.dot} />
);

export default Dot;

const RequiredMark: React.FC<{ label: React.ReactNode; required: boolean }> = ({
  label,
  required,
}) => {
  return required ? (
    <span>
      {label}{' '}
      <span style={{ color: '#FF4D4F' }}>*</span>
    </span>
  ) : (
    <span>{label}</span>
  );
};


const renderRequiredMark: (label: React.ReactNode, config: { required: boolean }) => React.ReactNode = (label, { required }) => (
  <RequiredMark label={label} required={required} />
);

export default renderRequiredMark;

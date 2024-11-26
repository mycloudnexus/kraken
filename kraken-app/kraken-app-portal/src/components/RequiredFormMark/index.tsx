
const RequiredMark: React.FC<{ label: React.ReactNode; required?: boolean; suffix?: React.ReactNode }> = ({
  label,
  required = false,
  suffix = required ? <span style={{ color: '#FF4D4F' }}>*</span> : null,
}) => {
  return (
    <span>
      {label} {suffix}
    </span>
  );
};


const renderRequiredMark: (label: React.ReactNode, config: { required: boolean }) => React.ReactNode = (label, { required }) => (
  <RequiredMark label={label} required={required} />
);

export default renderRequiredMark;

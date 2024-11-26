import { ReactNode } from 'react';

const RequiredMark = ({ label, required }: { label: ReactNode, required: boolean }) => {
  return required ? (
    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
      {label}{" "}
      <span className="required-label" style={{ color: "#FF4D4F" }}>
        *
      </span>
    </div>
  ) : (
    <span>{label}</span>
  );
};

export default RequiredMark;

import DOMPurify from "dompurify";

type Props = {
  text?: string;
  className?: string;
};

const RichTextViewer = ({ text = "", className }: Props) => {
  const formattedText = text.replace(/\n/g, "<br />");
  const cleanHTML = DOMPurify.sanitize(formattedText);
  return (
    <div
      className={className}
      dangerouslySetInnerHTML={{ __html: cleanHTML }}
    />
  );
};

export default RichTextViewer;

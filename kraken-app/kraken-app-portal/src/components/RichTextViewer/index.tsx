import DOMPurify from "dompurify";
import { HTMLAttributes } from "react";

const RichTextViewer = ({
  text = "",
  ...props
}: Readonly<HTMLAttributes<HTMLDivElement>> & { text: string }) => {
  const formattedText = text.replace(/\n/g, "<br />");
  const cleanHTML = DOMPurify.sanitize(formattedText);
  return <div {...props} dangerouslySetInnerHTML={{ __html: cleanHTML }} />;
};

export default RichTextViewer;

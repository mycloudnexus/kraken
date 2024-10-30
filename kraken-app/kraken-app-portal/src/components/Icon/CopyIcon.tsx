import Icon from "@ant-design/icons";
import type { GetProps } from "antd";

type CustomIconComponentProps = GetProps<typeof Icon>;

const CopySVG = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width={16} height={16} fill="none">
    <g fill="#2962FF" clipPath="url(#a)">
      <path d="M13.723 15.334H5.865a1.635 1.635 0 0 1-1.636-1.636V5.865a1.635 1.635 0 0 1 1.636-1.636h7.858a1.635 1.635 0 0 1 1.635 1.636v7.858c-.024.878-.757 1.61-1.635 1.61Zm-7.858-9.64a.167.167 0 0 0-.17.17v7.859c0 .097.072.17.17.17h7.858a.167.167 0 0 0 .17-.17V5.865a.167.167 0 0 0-.17-.17H5.865v-.002Z" />
      <path d="M3.01 11.77h-.708a1.635 1.635 0 0 1-1.635-1.635V2.302A1.635 1.635 0 0 1 2.302.667h7.858a1.635 1.635 0 0 1 1.635 1.635v.78a.72.72 0 0 1-.732.732.72.72 0 0 1-.732-.732v-.78a.166.166 0 0 0-.17-.171H2.301a.167.167 0 0 0-.171.17v7.859c0 .097.073.17.171.17h.708a.72.72 0 0 1 .732.732c0 .415-.342.709-.732.709Z" />
    </g>
    <defs>
      <clipPath id="a">
        <path fill="#fff" d="M0 0h16v16H0z" />
      </clipPath>
    </defs>
  </svg>
);

const CopyIcon = (props: CustomIconComponentProps) => (
  <Icon {...props} component={CopySVG} />
);
export default CopyIcon;

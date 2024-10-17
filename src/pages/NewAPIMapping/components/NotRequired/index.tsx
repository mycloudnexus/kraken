import { Flex } from "antd";
import styles from "./index.module.scss";
import { Text } from "@/components/Text";
const NotRequired = () => {
  return (
    <Flex vertical className={styles.root}>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width={88}
        height={114}
        fill="none"
      >
        <g opacity={0.7}>
          <g filter="url(#a)">
            <path
              fill="#000"
              d="M66.753 9.055h-45.14a9.064 9.064 0 0 0-9.064 9.064v69.76a9.064 9.064 0 0 0 9.064 9.064h45.14a9.064 9.064 0 0 0 9.064-9.065V18.12a9.064 9.064 0 0 0-9.064-9.064Z"
            />
          </g>
          <path
            fill="#fff"
            d="M66.83 8.647H21.17c-5.065 0-9.17 4.11-9.17 9.18v70.641c0 5.07 4.105 9.18 9.17 9.18h45.66c5.065 0 9.17-4.11 9.17-9.18V17.826c0-5.069-4.105-9.179-9.17-9.179Z"
          />
          <path
            fill="#fff"
            fillRule="evenodd"
            d="M18.91 21.743h50.542c.667 0 1.208.542 1.208 1.21v65.26c0 .668-.541 1.209-1.208 1.209H18.91a1.208 1.208 0 0 1-1.208-1.209v-65.26c0-.669.541-1.21 1.208-1.21Z"
            clipRule="evenodd"
          />
          <path
            fill="#389E0D"
            d="M55.735 81.618a7.22 7.22 0 0 0 7.222-7.22 7.22 7.22 0 0 0-7.222-7.218 7.22 7.22 0 0 0-7.223 7.219 7.22 7.22 0 0 0 7.223 7.22Z"
          />
          <path
            fill="#fff"
            d="M54.577 77.077a.7.7 0 0 0 .994 0l3.515-3.525a.706.706 0 0 0 0-.997.701.701 0 0 0-.994 0l-3.018 3.026-1.612-1.616a.7.7 0 0 0-.995 0 .706.706 0 0 0 0 .997l2.11 2.115Z"
          />
          <path
            fill="#ABAEB9"
            d="M61.516 37.253H26.845a1.645 1.645 0 1 0 0 3.29h34.671a1.645 1.645 0 1 0 0-3.29ZM61.517 44.773H26.845a1.645 1.645 0 1 0 0 3.29h34.672a1.645 1.645 0 0 0 0-3.29ZM61.517 52.763H26.845a1.645 1.645 0 1 0 0 3.29h34.672a1.645 1.645 0 0 0 0-3.29ZM40.427 69.213H26.845a1.645 1.645 0 0 0 0 3.29h13.582a1.645 1.645 0 1 0 0-3.29ZM33.865 75.792h-7.02a1.645 1.645 0 1 0 0 3.29h7.02a1.645 1.645 0 1 0 0-3.29Z"
          />
          <path
            stroke="#9DACBF"
            strokeWidth={1.734}
            d="M32.232 13.754h24.37v11.732a3.022 3.022 0 0 1-4.013 2.855l-7.182-2.493a3.022 3.022 0 0 0-1.982 0l-7.182 2.493a3.022 3.022 0 0 1-4.012-2.854l.001-11.733Z"
            clipRule="evenodd"
          />
          <path
            fill="#98A2B3"
            d="M58.598 4.354H29.763a3.625 3.625 0 0 0-3.625 3.625v4.498a3.625 3.625 0 0 0 3.625 3.626h28.835a3.625 3.625 0 0 0 3.626-3.626V7.98a3.625 3.625 0 0 0-3.626-3.625Z"
          />
        </g>
        <defs>
          <filter
            id="a"
            width={87.469}
            height={112.088}
            x={0.449}
            y={0.955}
            colorInterpolationFilters="sRGB"
            filterUnits="userSpaceOnUse"
          >
            <feFlood floodOpacity={0} result="BackgroundImageFix" />
            <feColorMatrix
              in="SourceAlpha"
              result="hardAlpha"
              values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
            />
            <feOffset dy={4} />
            <feGaussianBlur stdDeviation={6.05} />
            <feColorMatrix values="0 0 0 0 0.226735 0 0 0 0 0.437054 0 0 0 0 0.687819 0 0 0 0.189057 0" />
            <feBlend
              in2="BackgroundImageFix"
              result="effect1_dropShadow_4630_11305"
            />
            <feBlend
              in="SourceGraphic"
              in2="effect1_dropShadow_4630_11305"
              result="shape"
            />
          </filter>
        </defs>
      </svg>
      <Text.LightMedium color="#00000040">Not required.</Text.LightMedium>
    </Flex>
  );
};

export default NotRequired;

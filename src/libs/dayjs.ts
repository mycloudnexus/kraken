import {
  DAY_FORMAT,
  DAY_TIME_FORMAT_NORMAL,
  TIME_FORMAT,
} from "@/utils/constants/format";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";
import weekOfYear from "dayjs/plugin/weekOfYear";

dayjs.extend(weekOfYear);
dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.extend(relativeTime);

export const toDateTime = (
  time?: string | number,
  isShort = false,
  defaultReturn?: string
) => {
  const format = isShort ? DAY_FORMAT : DAY_TIME_FORMAT_NORMAL;
  if (!time) return defaultReturn;
  if (typeof time === "number")
    return dayjs.unix(time).utc().local().format(format);
  return dayjs.utc(time).local().format(format);
};

export const toTime = (time?: string | number, defaultReturn?: string) => {
  if (!time) return defaultReturn;
  if (typeof time === "number")
    return dayjs.unix(time).utc().local().format(TIME_FORMAT);
  return dayjs.utc(time).local().format(TIME_FORMAT);
};

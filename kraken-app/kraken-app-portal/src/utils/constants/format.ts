import dayjs from "dayjs";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";

export const DAY_TIME_FORMAT_NORMAL = "YYYY-MM-DD HH:mm:ss";
export const DAY_FORMAT = "YYYY-MM-DD";
export const TIME_FORMAT = "HH:mm:ss";
export const TIME_ZONE_FORMAT = "YYYY-MM-DDTHH:mm:ssZ";

export const getCurrentTimeWithZone = () => {
  dayjs.extend(timezone);
  dayjs.extend(utc);
  return dayjs().tz(dayjs.tz.guess()).format(TIME_ZONE_FORMAT);
};

export const formatDiagramDate = (value: any): string => {
  return dayjs(value).format("D-M");
};

export const recentXDays = (value?: number) => {
  if(!value) return {}

  const requestEndTime = getCurrentTimeWithZone();
  const requestStartTime =
   value === 7
      ? dayjs().subtract(7, "days").format(TIME_ZONE_FORMAT)
      : dayjs().subtract(3, "months").format(TIME_ZONE_FORMAT);
  return { requestStartTime, requestEndTime }
};

// 简单示例，返回「多久前」
export function formatTimeToNow(timestamp) {
  const delta = (Date.now() - new Date(timestamp).getTime()) / 1000
  if (delta < 60) return '刚刚'
  if (delta < 3600) return `${Math.floor(delta / 60)} 分钟前`
  if (delta < 86400) return `${Math.floor(delta / 3600)} 小时前`
  return `${Math.floor(delta / 86400)} 天前`
}
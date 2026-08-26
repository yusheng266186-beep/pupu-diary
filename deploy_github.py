#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""噗噗日记 GitHub 一键部署脚本。

用法：
    set GITHUB_TOKEN=ghp_xxx            # Windows CMD
    export GITHUB_TOKEN=ghp_xxx         # Git Bash
    可选代理：export GITHUB_PROXY=127.0.0.1:10808   (v2rayN SOCKS)
    python deploy_github.py

完成内容：
  1. 创建 GitHub 仓库 pupu-diary（已存在则跳过）
  2. 推送 main 分支（首次提交自动重置作者为你的 GitHub 账号）
  3. 为 v1.2.0 ~ v2.0.0 每个版本创建 Release，附对应优化说明
  4. 上传 dist/ 下各版本的 debug + release APK 作为 Release 资产
"""
import json
import os
import subprocess
import sys

REPO_NAME = "pupu-diary"
DESCRIPTION = "噗噗日记 · 极简治愈风的便便健康记录 Android App（Kotlin + Jetpack Compose，全手绘 Canvas 插画）"
TOPICS = ["android", "kotlin", "jetpack-compose", "compose", "health", "diary", "bristol-scale"]
API = "https://api.github.com"

# 每个版本的 Release 说明（与 DESIGN.md 更新日志一致）
RELEASES = [
    ("v1.2.0", "高保真对齐参考稿", """### 视觉
- 便便主角改为参考稿的三层软冰淇淋漩涡 + 卷曲尖，层间投影与高光
- Bristol 2–5 型统一为与主角同语言的迷你漩涡纹理
- 首页场景新增角落绿植、白色小花、草地肌理；统计页环形图分组图例 + Y 轴刻度 + 规律小发现
- 我的页设置行图标统一为圆形线性图标

### 功能
- 备份体系：导出 JSON 到下载目录 / 导入恢复（去重合并）/ 每周自动静默备份
- 提醒加固：开机广播自动重挂闹钟
- 记录增强：补记时间、备注、最近记录点击进入编辑；统计页新增可翻月月历
- Release 签名 + R8 混淆，体积 8.9MB → 1.2MB"""),
    ("v1.3.0", "还原度 + 动效 + 内容补全", """### 还原度
- 形状命名对齐参考稿（1 硬块状 … 7 水样状），区块标题改为「便形状 / 心情」
- 我的页重构为参考稿六行式（全部行末箭头 + 底部面板）

### 动效与内容
- 首页错峰入场动画、角色脚下柔影、闲时随机小跳、每日轮换健康语
- 贴士页新增「便形状速查」卡；打卡数值弹跳；底栏记录图标改对勾方框
- 版本号接入 BuildConfig，修复编辑页状态栏遮挡"""),
    ("v1.4.0", "融合「便便晴天」亮点，功能补全", """### 首页
- 时段问候；点击角色气泡对话（6 句轮换）+ 爱心迸发；最近一次记录卡片

### 记录与数据
- 未选形状保存 → 表单抖动提示；保存成功 → 撒花 + 「记录成功！」反馈卡
- 「更多记录」折叠区：排便时长快捷片 + 备注（DB v3，备份向后兼容）

### 统计
- 日/周/月/年四段（年 = 近 12 个月柱状图）；三指标总览；角色空状态"""),
    ("v1.5.0", "全流程审查修复 + 体验打磨", """- 开屏 1.9s 可点击跳过，开屏→主页交叉淡入
- 打卡新增减号回退按钮；顺滑度滑杆每跨一档轻触感
- 月历翻月方向滑动过渡；首页场景漂浮微光
- 修复抖动动画按像素计算幅度过小、气泡小屏被裁剪"""),
    ("v1.6.0", "首页结构对齐参考稿", """- 首页重构：问候语 → 大场景卡 → 底栏的极简结构
- 「记录便便」按钮移入场景卡内部、坐在草丘上（参考稿位置）
- 贴士入口改为场景左上角半透明小叶子徽章；区块标题改纯文字"""),
    ("v1.7.0", "数据流与生命周期审查", """### 重要修复
- **每日提醒只响一天**：一次性闹钟触发后不再调度，现已自动续期明天的提醒
- 编辑记录后统计图表不刷新（缓存键补数据戳）

### 新增
- 月历点击某天查看当日明细，可直接编辑/删除
- 每日打卡旧键自动清理；图标按钮补无障碍语义"""),
    ("v1.8.0", "商业级交付审查：链路闭环 + 边界加固", """- 通知点击直达对应页面（记录提醒→记录页）；singleTop 防止实例叠加
- 导出备份后一键系统分享（FileProvider 兼容 Android 9 以下）
- 本地崩溃日志，报障有据可查
- 修复时长芯片窄屏溢出、月历缓存时效；心情索引越界加固
- 贴士新增「连续不顺畅 / 突然变稀」应对卡"""),
    ("v1.9.0", "用户反馈专项：五处体验修复", """- 主按钮柔和化；统计范围与月历联动（弹回当月 + 范围高亮）
- 自定义时间选择器（步进 + 快捷时间片）替换安卓原生 TimePicker
- 接入 MaterialTheme 色板 + 统一 PupuDialog 弹窗（根除 Material 默认紫）
- 修复贴士页返回按钮被状态栏遮挡；全局 BackHandler 逐级返回，手势不再一划退桌面"""),
    ("v2.0.0", "按钮 / 日历 / 图标三项重做", """### 糖果按压按钮
- 「按钮面 + 深色底边」双层结构，按下时按钮面真实下沉 4dp

### 范围日历
- 日 = 今天单日大卡片；周 = 最近 7 天条；月 = 可翻月整月格；年 = 近 12 个月格子
- 四形态切换带缩放 + 淡入过渡；日/周/月均可点开当日明细

### 图标
- 贴士页系统 emoji 全部替换为主题色手绘线性图标（水滴/闪电/叶子/月亮/苹果/问号/水波）"""),
]

PROXY = os.environ.get("GITHUB_PROXY", "")


def curl(args, binary=None, data=None):
    cmd = ["curl", "-s", "--connect-timeout", "20"]
    if PROXY:
        cmd += ["--socks5-hostname", PROXY]
    cmd += args
    if data is not None:
        cmd += ["--data-binary", data]
    if binary is not None:
        cmd += ["--data-binary", "@" + binary]
    return subprocess.run(cmd, capture_output=True, text=True).stdout


def api(method, path, token, payload=None):
    url = API + path if not path.startswith("http") else path
    out = curl(
        ["-w", "\n%{http_code}",
         "-H", "Authorization: Bearer " + token,
         "-H", "Accept: application/vnd.github+json",
         "-X", method] + (["-H", "Content-Type: application/json"] if payload is not None else []),
        data=json.dumps(payload) if payload is not None else None,
    ) + "\n"
    body, code = out.rsplit("\n", 2)[0:2]
    try:
        return int(code), (json.loads(body) if body.strip() else {})
    except Exception:
        return int(code), body


def main():
    token = os.environ.get("GITHUB_TOKEN") or (sys.argv[1] if len(sys.argv) > 1 else "")
    if not token:
        sys.exit("请先设置环境变量 GITHUB_TOKEN，或作为参数传入")

    status, user = api("GET", "/user", token)
    if status != 200:
        sys.exit(f"token 校验失败: {status} {user}")
    login = user["login"]
    print(f"== GitHub 账号: {login}")

    # 1. 创建仓库（已存在则跳过）
    status, repo = api("POST", "/user/repos", token, {
        "name": REPO_NAME, "description": DESCRIPTION,
        "private": False, "has_wiki": False, "has_issues": True,
    })
    if status == 201:
        print(f"== 仓库已创建: {repo['html_url']}")
    elif status == 422:
        print("== 仓库已存在，跳过创建")
    else:
        sys.exit(f"创建仓库失败: {status} {repo}")
    full = f"{login}/{REPO_NAME}"

    # 2. 提交身份对齐 GitHub 账号，并重置首次提交作者
    subprocess.run(["git", "config", "user.name", login], check=True)
    subprocess.run(["git", "config", "user.email", f"{login}@users.noreply.github.com"], check=True)
    subprocess.run(["git", "commit", "--amend", "--no-edit", "--reset-author"], check=True)

    # 3. 推送 main
    push_url = f"https://x-access-token:{token}@github.com/{full}.git"
    push_cmd = ["git"]
    if PROXY:
        push_cmd += ["-c", "http.proxy=socks5h://" + PROXY]
    push_cmd += ["push", push_url, "main"]
    print("+", " ".join(push_cmd).replace(token, "***"))
    subprocess.run(push_cmd, check=True)
    print(f"== main 已推送: https://github.com/{full}")

    # 4. 仓库话题标签
    api("PUT", f"/repos/{full}/topics", token, {"names": TOPICS})

    # 5. 逐版本创建 Release 并上传 APK
    for tag, title, notes in RELEASES:
        body = notes + f"\n\n---\n📦 安装包见下方 Assets（release 为签名优化版，debug 为调试版）\n完整更新记录见 [DESIGN.md](https://github.com/{full}/blob/main/DESIGN.md)"
        status, rel = api("POST", f"/repos/{full}/releases", token, {
            "tag_name": tag, "name": f"{tag} · {title}", "body": body,
        })
        if status not in (201, 422):
            print(f"!! {tag} Release 创建失败: {status} {rel}")
            continue
        if status == 422:
            print(f"== {tag} Release 已存在，仅补传资产")
            status, rel = api("GET", f"/repos/{full}/releases/tags/{tag}", token)
        upload = rel["upload_url"].split("{")[0]
        for apk in (f"dist/pupu-diary-{tag[1:]}-release.apk", f"dist/pupu-diary-{tag[1:]}-debug.apk"):
            if not os.path.exists(apk):
                print(f"   (缺资产 {apk}，跳过)")
                continue
            name = os.path.basename(apk)
            code = curl(
                ["-o", os.devnull, "-w", "%{http_code}",
                 "-H", "Authorization: Bearer " + token,
                 "-H", "Content-Type: application/vnd.android.package-archive",
                 "-X", "POST", f"{upload}?name={name}"],
                binary=apk,
            )
            print(f"   ↑ {name} (HTTP {code})")
        print(f"== {tag} 完成: {rel.get('html_url', '')}")

    print(f"\n✅ 全部完成！仓库地址: https://github.com/{full}")


if __name__ == "__main__":
    main()

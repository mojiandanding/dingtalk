package org.devops

def GetChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ""
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            if (j > 10) {
                changeString += "......"
                break
            }
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            commitTime = new Date(entry.timestamp).format("yyyy-MM-dd HH:mm:ss")
            changeString += "·${truncated_msg} [${entry.author} ${commitTime}]\\n"
        }
    }
    if (!changeString) {
        changeString = "No new changes"
    }
    return changeString
}

def request_post(endpoint, comment) {
    println(comment)
    def url = new URL(endpoint)
    def url_connection = url.openConnection()

    url_connection.setRequestMethod("POST")
    url_connection.setDoOutput(true)
    url_connection.setRequestProperty("Content-Type", "application/json")

    url_connection.getOutputStream().write(comment.getBytes("UTF-8"))

    def response_code = url_connection.getResponseCode()
    println(response_code)
    if (response_code != 200) {
        println("Warning: faled to post message")
    }
    else {
        println("successed to post message")
    }
}

def GetSuccessContent()
{
  wrap([$class: 'BuildUser']) {
    def Content = ""
	  Content = "项目名称：**${env.JOB_NAME}**\\n当前版本：**${env.BUILD_NUMBER}**\\n构建发起：**${env.BUILD_USER}**\\n持续时间：**${currentBuild.durationString}**\\n构建日志：**[点击查看详情](${env.BUILD_URL}console)**\\n构建结果：**构建成功 ✅**"
    return Content
	} 
}

def GetFailureContent()
{
  wrap([$class: 'BuildUser']) {
    def Content = ""
	  Content = "项目名称：**${env.JOB_NAME}**\\n当前版本：**${env.BUILD_NUMBER}**\\n构建发起：**${env.BUILD_USER}**\\n持续时间：**${currentBuild.durationString}**\\n构建日志：**[点击查看详情](${env.BUILD_URL}console)**\\n构建结果：**构建失败 ❌**"
    return Content
	} 
}

def GetMsgData(content, color) {
  def msg_data = """
  {
    "msg_type":"interactive",
    "card":{
      "config":{
        "wide_screen_mode": true,
        "enable_forward": true
      },
      "elements":[
        {
          "tag": "div",
          "text": {
            "content": "${content}",
            "tag": "lark_md"
          }
        },
        {
          "tag": "hr"
        },
        {
          "tag": "div",
          "text": {
            "content": "更新记录：\\n${GetChangeString()}",
            "tag": "lark_md"
          }
        }
      ],
      "header":{
        "template": "${color}",
        "title":{
          "content": "Jenkins构建报告",
          "tag": "plain_text"
        }
      }
    }
  }
  """
	
  return msg_data
}

def SuccessNotice(wehook) {
  def content = GetSuccessContent()
  def msg_data = GetMsgData(content, "green")
  request_post(wehook, msg_data)   
}

def FailureNotice(wehook) {
  def content = GetFailureContent()
  def msg_data = GetMsgData(content, "red")
  request_post(wehook, msg_data)
}

import request from "@/utils/request.js"
const AI_API_PREFIX = "/ct"
// AI 接口响应较慢（LLM 生成需 5~20s），单独放宽超时，避免默认 5s 超时导致"获取对话失败"
const AI_TIMEOUT = 120000
const aiRequest = (config) => request({ timeout: AI_TIMEOUT, ...config })

// 用户会话模块

//新增会话
export const createUserSession = (data) =>
    aiRequest({
        url: `${AI_API_PREFIX}/session`,
        method: 'post',
        data
    })
//查询用户会话列表
export const getUserSessionList = () =>
    aiRequest({
        url: `${AI_API_PREFIX}/session/list`,
        method: 'get',
    })
//更改对话
export const updateUserSession = (id,params) =>
    aiRequest({
        url: `${AI_API_PREFIX}/session/${id}`,
        method: 'put',
        params
    })
//删除会话
export const deleteUserSession = (id) =>
    aiRequest({
        url: `${AI_API_PREFIX}/session/${id}`,
        method: 'delete',
    })

//聊天接口

//根据会话id获取聊天记录
export const getChatRecord = (params) =>
    aiRequest({
        url: `${AI_API_PREFIX}/chat/records`,
        method: 'get',
        params
    })

// AI聊天接口
export const memoryChatRedis = (params) =>
    aiRequest({
        url: `${AI_API_PREFIX}/chat/simple`,
        method: 'get',
        params
    })
//AI流式聊天接口
export const memoryChatRedisStream = (params) =>
    aiRequest({
        url: `${AI_API_PREFIX}/chat/`,
        method: 'get',
        params,
        responseType: 'stream' 
    })

// 知识库接口

// 上传文件到知识库
export const uploadMarkdown = (file, level = 2) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('level', level);
    return aiRequest({
    url: `${AI_API_PREFIX}/file/upload`,
    method: 'post',
    data: formData,
    headers: {
        'Content-Type': 'multipart/form-data'
    }
    });
};

// 根据知识库内容对话
export const chatByMarkdownDoc = (params) =>
    aiRequest({
    url: `${AI_API_PREFIX}/file/chat`,
    method: 'get',
    params
    });

// 分页查询用户知识库文件列表
export const queryMarkdownPage = (query) =>
    aiRequest({
    url: `${AI_API_PREFIX}/file/page`,
    method: 'get',
    query
    });

// 根据文件 id 查看文件内容
export const getMarkdown = (fileId) =>
    aiRequest({
    url: `${AI_API_PREFIX}/file/${fileId}`,
    method: 'get'
    });

// 更新文件内容
export const updateMarkdown = (markdownDocs) =>
    aiRequest({
    url: `${AI_API_PREFIX}/file/update`,
    method: 'put',
    data: markdownDocs
    });

// 根据文件 id 删除文件
export const deleteMarkdown = (fileId) =>
    aiRequest({
    url: `${AI_API_PREFIX}/file/${fileId}`,
    method: 'delete'
});
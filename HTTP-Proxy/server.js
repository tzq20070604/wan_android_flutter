
const express = require('express');  
const { createProxyMiddleware,responseInterceptor } = require('http-proxy-middleware');  
const https = require('https');  
const cors = require('cors');  
const fs = require('fs');  
const app = express();  
  
// HTTPS配置  
const httpsOptions = {  
  key: fs.readFileSync('./localhost-key.pem'),  
  cert: fs.readFileSync('./localhost.pem')  
};  
  
// HTTPS服务器  
const server = https.createServer(httpsOptions, app);  

//
// app.use(cors({  
//     origin: '*'  
//   })); 
// 自定义CORS中间件  
app.use((req, res, next) => {  
  
  const origin = req.headers.origin;  
  // console.log(origin)
  // console.log(res)
  if (origin?.length > 0) {
    res.setHeader('Access-Control-Allow-Origin', origin);  
  }
  res.setHeader('Access-Control-Allow-Credentials', 'true');  
  if (req.method === 'OPTIONS') {  
    // 预检请求的响应  
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');  
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');  
    res.setHeader('Access-Control-Allow-Credentials', 'true');  
    res.status(204).send('');  
  } else {  
    next();  
  }  
});  

// 反向代理配置  
app.use(   
  createProxyMiddleware({  
    target: 'https://www.wanandroid.com/', // 你的后端服务地址  
    changeOrigin: true, // 是否跨域  
    // selfHandleResponse: false, // 如果需要自定义响应处理，则设置为true  
    // 其他HTTPS相关配置（如果需要）  
    // 注意：大多数HTTPS相关的配置应已在httpsOptions中处理  
    // 但如果需要转发HTTPS特定的请求头，可以在这里指定  
    // onProxyReq: function(proxyReq, req, res) {  
    //   // 例如，设置请求头  
    // //   proxyReq.setHeader('X-Custom-Header', 'value');  
    // }, 
    // 其他配置...
    //  onProxyRes: function(proxyRes, req, res) {  
    //     console.log(res)
    //     console.log(proxyRes)
    //     // 在这里添加CORS响应头  
    //     // 注意：这可能会与目标服务器的CORS设置冲突  
    //     // res.setHeader('Access-Control-Allow-Origin', '*');  
        
    //     // 可能还需要添加其他CORS相关的响应头，如Access-Control-Allow-Methods, Access-Control-Allow-Headers等  
    //  }  
    selfHandleResponse: true,
    on: {
      proxyReq: (proxyReq, req, res) => {
        console.log('proxyReq:', req.headers);
      },
      proxyRes: responseInterceptor(async (responseBuffer, proxyRes, req, res) => {
       let cookieArr = res.getHeaders()["set-cookie"]
       console.log(cookieArr);
       if (cookieArr) {
            let newArr = cookieArr.map((string)=>{
              string = string + ';Secure; HttpOnly;SameSite=none';
              let regex = /anandroid\.com/;
              let newStr = string.replace(regex, 'localhost:3000')
              return newStr
          })
          res.setHeader('set-cookie', newArr); // Set a new header and value
          res.cookie('rememberme', '1', { maxAge: 900000, httpOnly: true, secure:true,sameSite:"none" });
       }
        return responseBuffer;
      }),
    },
  })  
);  
  
// 监听端口  
server.listen(3000, () => {  
  console.log('HTTPS服务器正在监听端口3000');  
});
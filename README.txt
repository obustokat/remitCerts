# FTP 
Download FileZilla Client、FileZilla Server
https://filezilla-project.org/

教学看这两篇: 若要改成连到自己FTP 需要先设置 FTP Server 并创建使用者
https://blog.tarswork.com/post/filezilla-server-install-and-usage
https://blog.hungwin.com.tw/ftp-filezilla/

# ngrok
ngrok 的转发（Forwarding）功能的确是让外网通过指定的 ngrok 地址（URL）访问你本地的服务。
例如，如果你在本地运行了一个服务在 localhost:8080，并通过 ngrok 创建了一个隧道，你会看到类似这样的输出
ngrok http 5678
Forwarding                    http://abcd1234.ngrok.io -> http://localhost:8080

意味着外网用户可以通过访问 http://abcd1234.ngrok.io 来连接到你本地的 http://localhost:8080 服务。


1. ERROR:  authentication failed: Usage of ngrok requires a verified account and authtoken.
ngrok 要求你使用已验证的帐户和 authtoken。
# 安装你的 authtoken
ngrok authtoken YOUR_AUTHTOKEN

2. ERROR:  authentication failed: Your account is limited to 1 simultaneous ngrok agent sessions.
Using these methods,terminate an ngrok session when it is no longer needed.

curl http://127.0.0.1:4040/api/tunnels
# {"tunnels":[{"name":"command_line","
curl -X DELETE http://127.0.0.1:4040/api/tunnels/{tunnel_name}

# 流程
寫入文件 -> 壓縮文件 -> 上傳FTP -> 取得ngrok forward (映射localhost port) -> 返回下載連結

寫入文件
1. call api 取rsa公私钥(预设-密钥长度: 1024bit,密钥格式: PKCS#1) ,md5key(预设-16位小写) (这步可能会因为网站挂到而导致值是null
(TODO: 掛了要切成自己產)
2. 取得api数据，并将其余参数: 平台公钥组完产生服务器资料写入档案
(TODO: 通过流的方式可以直接将数据从输入流上传到FTP服务器，从而避免将文件临时存储在本地)

壓縮文件
1. 压缩文件固定格式 daifu000/merchantNo/checkOrderKey.txt (写入 当前工作目录 + remitCerts/temp/)

取得zip档案上传ftp
ftp位置: D:\FTP\user1\certs (virtual path= / ,Native path= D:/FTP)

POST application/json http://localhost:5678/api/getDaifuRSAFile
{
	"rsaThirdPubKey": "123",
	"daifuId": "daifu001",
	"merchantNo": "70012"
}

{
	"rsaLength": "4012",
	"rsaFormat": "PKCS#8",
	"rsaThirdPubKey": "123",
	"daifuId": "daifu001",
	"merchantNo": "70012"
}

curl:
curl -X POST http://localhost:5678/api/getDaifuRSAFile \
-H "Content-Type: application/json" \
-d '{
    "rsaThirdPubKey": "qwmkdm1no123no21nflm334jvio34j53p5ki2-0v5ik1kv124vk12[p4k124njhiot3cn4pctn43ptcm34octm34pojbp3op5jnp2ojn1[23por[ewr23r424365r42365r4236",
    "daifuId": "daifu005",
    "merchantNo": "70153"
}'

Each time, the returned ngrok forwarding address is different.
curl -OJL -X GET https://b7eb-2001-b400-e450-e110-4474-5d24-6ac8-a246.ngrok-free.app/api/ftp_files/daifu005.zip

file content:
rsaThirdPubKey=123
rsaPrivateKey=MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM8L/FkgP2+3T7n7zHOfp9XNFv2mbYDGkzmUeqjMLMlEqq1nn9VpasxPWfFNZ6dnrD6TMCR9JiaDRGjp/1gW3crf5eXQL9M5xV8lXb2pmXwXTZRtTOkUdyGAG/+egx7N9+esYvrhYBV2kRqKccNRKc9MdC/LKri0oF6k363Xbw49AgMBAAECgYBnvWSqL3s5eYrL8Dsnr9ZDTOh5HRt+y4BW+GthCjRigDJYgjRWlOQ7DINPYW5PNaXbEJqtnbswztLHEn+rXoGvzzjK4mBiwSsMq6EvOYfw+fUQUfRWnossrVxhsD+S9RpOb6tWKErQ98SGX4j8htFH/GZAz5iAJUIPCUv/buzbLQJBAOh09TAz5djNSlswyxjiKpxJv3UbjeamkZvDClHjbS8b/GbpIvXbyrJBeDAufoGTsWOCkmvRcDh/CEJegzDAY48CQQDkBDUGxfoy80ezX4eW85/06gpRZfOt1/V2hgrluKPC+ewFHv0+I85CQsEDnUItZe5HQkt1zo3JQVqJjgw/OdtzAkByXjjUAnSvejF/ND54e63jPxWHlIr4VDOHoypMp8xsRSSlrUaaGA1eLn/nlLiBIB1CjFdl3KGN6lIx9TYuojlZAkEAi8Q4bMp2Lz0Iul7YUiNL7Wh4oqSH1gwGnnxTubSQTTe9APaZ7Lkt+VfJ0FlY81MD2BI/Bxtxteelg6PaxfbFGQJAbeRJhO+qFZj0OK1RI3UQvOOLeH+ktnZCQQm4NT1oBAo4V0I4+kE2jcornoaIyb4Si4Z/HpKNdJ10aarNN3YuwQ==
rsaPubKey=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDPC/xZID9vt0+5+8xzn6fVzRb9pm2AxpM5lHqozCzJRKqtZ5/VaWrMT1nxTWenZ6w+kzAkfSYmg0Ro6f9YFt3K3+Xl0C/TOcVfJV29qZl8F02UbUzpFHchgBv/noMezffnrGL64WAVdpEainHDUSnPTHQvyyq4tKBepN+t128OPQIDAQAB
md5Key=df84bf8cf4c44caeede3e10fcbe023ab

/api/getDaifuRSAFile Body:
{
    "fileName": "https://b7eb-2001-b400-e450-e110-4474-5d24-6ac8-a246.ngrok-free.app/api/ftp_files/daifu005.zip",
    "message": "File uploaded successfully"
}
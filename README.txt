Download FileZilla Client、FileZilla Server
https://filezilla-project.org/

教学看这两篇: 若要改成连到自己FTP 需要先设置 FTP Server 并创建使用者
https://blog.tarswork.com/post/filezilla-server-install-and-usage
https://blog.hungwin.com.tw/ftp-filezilla/

资料夹:
1. git clone 同层需要建立Temp 的资料夹存放写入档案跟压缩档
2. ftp 路径   remotePath: /user1/certs/ 写死也要建立

执行顺序: 
step1: call api 取rsa公私钥 ,md5key (可能会因为网站挂到而导致值是null或其他错误)
step2: 整理数据，并将其余参数: 平台公钥组完产生服务器资料
step3: 服务器资料写入档案
step4: 压缩文件固定格式 daifu000/merchantNo/checkOrderKey.txt (最外层要有Temp资料夹存放写入跟压缩档)
step5: 取得zip档案上传ftp

POST application/json http://10.25.12.171:8080/api/getDaifuRSAFile
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

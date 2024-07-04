Download FileZilla Client、FileZilla Server
https://filezilla-project.org/

教学看这两篇: 若要改成连到自己FTP 需要先设置 FTP Server 并创建使用者
https://blog.tarswork.com/post/filezilla-server-install-and-usage
https://blog.hungwin.com.tw/ftp-filezilla/

资料夹:
1. git clone 同层需要建立Temp 的资料夹存放写入档案跟压缩档
2. ftp 路径   remotePath: /user1/certs/ 写死也要建立

执行顺序: 
step1: call api 取rsa公私钥(预设-密钥长度: 1024bit,密钥格式: PKCS#1) ,md5key(预设-16位小写) (这步可能会因为网站挂到而导致值是null)
step2: 整理数据，并将其余参数: 平台公钥组完产生服务器资料
step3: 服务器资料写入档案
step4: 压缩文件固定格式 daifu000/merchantNo/checkOrderKey.txt (最外层要有Temp资料夹存放写入跟压缩档)
step5: 取得zip档案上传ftp

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

结果:
rsaThirdPubKey=123
rsaPrivateKey=MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM8L/FkgP2+3T7n7zHOfp9XNFv2mbYDGkzmUeqjMLMlEqq1nn9VpasxPWfFNZ6dnrD6TMCR9JiaDRGjp/1gW3crf5eXQL9M5xV8lXb2pmXwXTZRtTOkUdyGAG/+egx7N9+esYvrhYBV2kRqKccNRKc9MdC/LKri0oF6k363Xbw49AgMBAAECgYBnvWSqL3s5eYrL8Dsnr9ZDTOh5HRt+y4BW+GthCjRigDJYgjRWlOQ7DINPYW5PNaXbEJqtnbswztLHEn+rXoGvzzjK4mBiwSsMq6EvOYfw+fUQUfRWnossrVxhsD+S9RpOb6tWKErQ98SGX4j8htFH/GZAz5iAJUIPCUv/buzbLQJBAOh09TAz5djNSlswyxjiKpxJv3UbjeamkZvDClHjbS8b/GbpIvXbyrJBeDAufoGTsWOCkmvRcDh/CEJegzDAY48CQQDkBDUGxfoy80ezX4eW85/06gpRZfOt1/V2hgrluKPC+ewFHv0+I85CQsEDnUItZe5HQkt1zo3JQVqJjgw/OdtzAkByXjjUAnSvejF/ND54e63jPxWHlIr4VDOHoypMp8xsRSSlrUaaGA1eLn/nlLiBIB1CjFdl3KGN6lIx9TYuojlZAkEAi8Q4bMp2Lz0Iul7YUiNL7Wh4oqSH1gwGnnxTubSQTTe9APaZ7Lkt+VfJ0FlY81MD2BI/Bxtxteelg6PaxfbFGQJAbeRJhO+qFZj0OK1RI3UQvOOLeH+ktnZCQQm4NT1oBAo4V0I4+kE2jcornoaIyb4Si4Z/HpKNdJ10aarNN3YuwQ==
rsaPubKey=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDPC/xZID9vt0+5+8xzn6fVzRb9pm2AxpM5lHqozCzJRKqtZ5/VaWrMT1nxTWenZ6w+kzAkfSYmg0Ro6f9YFt3K3+Xl0C/TOcVfJV29qZl8F02UbUzpFHchgBv/noMezffnrGL64WAVdpEainHDUSnPTHQvyyq4tKBepN+t128OPQIDAQAB
md5Key=df84bf8cf4c44caeede3e10fcbe023ab
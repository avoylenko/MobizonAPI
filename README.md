# MobizonAPI

MobizonAPI is a Java client for Mobizon services, which gives you an easy integration point.

[API Documentation] (https://mobizon.ua/integration/api)

## Usage

MobizonApi api = new MobizonApi("token", "api.mobizon.ua");
System.out.println(api.getOwnBalance());
System.out.println(api.sendSMSMessage("Hello from the other side", "380671234567"));

## Open source agreement

[MIT](https://github.com/biezhi/wechat-api/blob/master/LICENSE).

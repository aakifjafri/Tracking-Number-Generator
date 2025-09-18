# Tracking-Number-Generator

**Url to access the response**

https://tracking-generator-production.up.railway.app/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32+08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics

**cURL for the same**
curl --location --request GET 'https://tracking-generator-production.up.railway.app/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32+08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics' \
--header 'Accept: application/json'

Just hit this and you will get response in below format with tracking_number and created_at

{
    "tracking_number": "E08S13IELZJO",
    "created_at": "2025-09-18T09:59:52.866507738Z"
}

<?php

require_once 'sdp_services_config.php';

$time  = "1408959098";
//$spId = "200033";
//$serviceId = "0303233019";
$spPassword = "Robi1234";
//$pass = md5("200033Robi1234$time");
$timestamp = "1408959098";
//$ProductID="0300398904";

//subscribe.php?package_id=5day&bno=2828&msisdn=&msisdn

$package_id = trim($_REQUEST['package_id']);
$shortcode=$_REQUEST['bno'];
 
date_default_timezone_set("Asia/Dhaka");
$fromdate= date("Ymd",time()).'000001';
$fromdate= '20140114000001';
 
$todate=date("YmdHis",time());
//for every shortcode shoul maped in config file
$productIDlist=array("0300402070","0300402072","0300402074","0300402076", "0300406060");

// test // $productIDlist=array("030ffsd0","030040fdsfds72","03004dsgfgfh4","0300truytuty6");

if($package_id<>"")
{
//$shortcode_service="shortCode_$shortcode_$package_id";
	$shortcode_service="shortCode"."_".$shortcode."_".$package_id;

}
else
{
	$shortcode_service="shortCode_$shortcode";

}

if(array_key_exists($shortcode_service, $sdp_service_info))
{
	$spId 		= $sdp_service_info[$shortcode_service]['spId'];
	$serviceId 	= $sdp_service_info[$shortcode_service]['serviceId'];
	$ProductID 	= $sdp_service_info[$shortcode_service]['ProductID'];
	$shortcode_name	= $sdp_service_info[$shortcode_service]['shortcode'];
	$servicename	= $sdp_service_info[$shortcode_service]['servicename'];
	$Amount	= $sdp_service_info[$shortcode_service]['Amount'];
	
}

$pass = md5($spId."Robi1234".$time);

$msisdn = trim($_REQUEST['msisdn']);

$msisdn=substr($msisdn,-10);
$code=substr($msisdn,0,2);

$Ano=$msisdn;

if($code !='18'  and $code !='16')
{
	echo 2; // 0 chilo existing kajer violation er jonno 2 kora holo
	exit;
}


$msisdn = "880".$msisdn;

$strXML 	= '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:loc="http://www.sdp.com/schema/subscription/v1_0/local"><soapenv:Header>
      <tns:RequestSOAPHeader xmlns:tns="http://www.huawei.com.cn/schema/common/v2_1">
         <tns:spId>'.$spId.'</tns:spId>
         <tns:spPassword>'.$pass.'</tns:spPassword>
         <tns:timeStamp>'.$time.'</tns:timeStamp>
      </tns:RequestSOAPHeader>
   </soapenv:Header>
   <soapenv:Body>
      <loc:getSubScriptionListReq>
         <loc:userID>
            <ID>'.$msisdn.'</ID>
            <type>0</type>
         </loc:userID>
         <loc:actionType>1</loc:actionType>
         <!--Optional:-->
         <loc:langInfo>
            <language>en</language>
            <!--Optional:-->
            <country>US</country>
         </loc:langInfo>   
         <!--Optional:-->
         <loc:recordIndex>1</loc:recordIndex>
         <!--Optional:-->
         <loc:batchSize>10</loc:batchSize>
         <!--Optional:-->
         <loc:extensionInfos>
            <!--Zero or more repetitions:-->
            <loc:item>
               <key>fromDate</key>
               <value>'.$fromdate.'</value>
            </loc:item>
            <loc:item>
               <key>toDate</key>
               <value>'.$todate.'</value>
            </loc:item>
            <loc:item>
               <key>sequenceType</key>
               <value>0</value>
            </loc:item>
            <loc:item>
               <key>sequenceCondition</key>
               <value>3</value>
            </loc:item>
            <loc:item>
               <key>language</key>
               <value>en_US</value>
            </loc:item>
         </loc:extensionInfos>
      </loc:getSubScriptionListReq>
   </soapenv:Body>
</soapenv:Envelope>';

date_default_timezone_set("Asia/Dhaka");
$fileName ="SDP_REQ_RES_".(string)date("Y_m_d_H",time()).".txt";
$logmsg="--------------------session Start-----------------------------".PHP_EOL;
$logmsg  .= "--------------------Request----------------".date("Y-m-d H:i:s",time()).PHP_EOL;
$logmsg.=PHP_EOL.$strXML.PHP_EOL;

$params = array('http' => array(
	'method' => 'POST',
	'content' => $strXML
));

$requestedXML	= stream_context_create($params);


$response= @file_get_contents("http://10.16.100.194:8310/SubScriptionService/services/SubScription", false, $requestedXML);

$logmsg.=PHP_EOL."--------------------Response----------------".date("Y-m-d H:i:s",time()).PHP_EOL.$response.PHP_EOL;
$logmsg.="--------------------session End.".PHP_EOL.PHP_EOL;
$file = fopen("logs/".$fileName,'a+');
fwrite($file, $logmsg);
fclose($file);

$data =$response;

$p = xml_parser_create();
xml_parse_into_struct($p, $data, $vals, $index);
xml_parser_free($p);

//echo "<pre>";
$response = preg_replace("/(<\/?)(\w+):([^>]*>)/", "$1$2$3", $response);
$xml = new SimpleXMLElement($response);
$body = $xml->xpath('//soapenvBody')[0];
$array = json_decode(json_encode((array)$body), TRUE); 
//print_r($array);

//echo $recordSum = $array['ns1getSubScriptionListRsp']['ns1recordSum'];

$recordSum = $array['ns1getSubScriptionListRsp']['ns1recordSum'];

//print_r ($array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']);

/*
for($i=0;$i<$recordSum; $i++)
{
echo "<pre>" ."</br>";

if($recordSum==1){

print_r ($array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]);
echo $i.'. userID:'.$userID= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1userID'];
echo "<pre>";
echo $i.'. productID:'.$productID= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1productID'];
echo "<pre>";
echo $i.'. subTime:'.$subTime= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1subTime'];
echo "<pre>";
echo $i.'. nextChargeTime:'.$nextChargeTime= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1nextChargeTime'];
echo "<pre>";
echo $i.'. subScriptionState:'.$subScriptionState= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1subScriptionState'];
echo "<pre>";
echo $i.'. isAutoExtend:'.$isAutoExtend= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1isAutoExtend'];
echo "<pre>";

}
else{

echo $i.'. userID:'.$userID= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1userID'];
echo "<pre>";
echo $i.'. productID:'.$productID= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1productID'];
echo "<pre>";
echo $i.'. subTime:'.$subTime= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1subTime'];
echo "<pre>";
echo $i.'. nextChargeTime:'.$nextChargeTime= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1nextChargeTime'];
echo "<pre>";
echo $i.'. subScriptionState:'.$subScriptionState= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1subScriptionState'];
echo "<pre>";
echo $i.'. isAutoExtend:'.$isAutoExtend= $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1isAutoExtend'];
echo "<pre>";
}
}

*/

if($recordSum<1){
	//echo 'go for Registration at srcmn 0.';
	echo 0;
}
else {

	for ($i = 0; $i < $recordSum; $i++) {
		if($recordSum==1){
			$productID = $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1productID'];
			$subScriptionState = $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo']['ns1subScriptionState'];
		}
		else{
			$productID = $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1productID'];
			$subScriptionState = $array['ns1getSubScriptionListRsp']['ns1subScriptionInfos']['ns1subScriptionInfo'][$i]['ns1subScriptionState'];
		}
		if (in_array($productID, $productIDlist)) {

			//if ($subScriptionState != 1) {
			if ($subScriptionState == 0) {	
				//echo "Go for Reg at SubState.";
				echo 0;
				break;

			} else {

				echo 1; // already registered
				break;
			}

		} else {
			//for check next array
			continue;

		}
	}
}
if($i == $recordSum)
{
	echo 0;
}

/*
$sep   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
	$file = fopen("trace_Subscription.txt",'a');
    
   	fwrite($file, $sep.date("Y-m-d H:i:s",time()).$sep.PHP_EOL.$strXML.PHP_EOL);

	fwrite($file, $sep.date("Y-m-d H:i:s",time()).$sep.PHP_EOL.$data.PHP_EOL);

	fclose($file);
*/
   

?>
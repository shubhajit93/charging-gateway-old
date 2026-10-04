<?php
require_once 'sdp_services_config.php';

$time  = "1408959098";
 
$spPassword = "Robi1234";
 
$timestamp = "1408959098";
 

$isForNonAuto=$_REQUEST['isForNonAuto'];



$package_id = trim($_REQUEST['package_id']);
$shortcode=$_REQUEST['bno'];
$msisdn = trim($_REQUEST['msisdn']);
$msisdn=substr($msisdn,-10);
$code=substr($msisdn,0,2);
 

if($code !='18' and  $code !='16')
{
  echo 0;
  exit;
}

 if(isset($isForNonAuto) && $isForNonAuto<>""){	

	if($package_id<>"") {
      		$shortcode_service="shortCode"."_".$shortcode."_".$package_id."_nonAuto";
   	}
   	else {
     		 $shortcode_service="shortCode_".$shortcode."_nonAuto";
   	}
		
}
else{

   if($package_id<>"") {
      $shortcode_service="shortCode"."_".$shortcode."_".$package_id;
   }
   else{
      $shortcode_service="shortCode_".$shortcode;
   }
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



$msisdn = "880".$msisdn;

$strXML 	= '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:loc="http://www.csapi.org/schema/parlayx/subscribe/manage/v1_0/local"><soapenv:Header><tns:RequestSOAPHeader xmlns:tns="http://www.huawei.com.cn/schema/common/v2_1"><tns:spId>'.$spId.'</tns:spId><tns:spPassword>'.$pass.'</tns:spPassword><tns:timeStamp>'.$timestamp.'</tns:timeStamp></tns:RequestSOAPHeader></soapenv:Header><soapenv:Body><loc:unSubscribeProductRequest><loc:unSubscribeProductReq><userID><ID>'.$msisdn.'</ID><type>0</type></userID><subInfo><productID>'.$ProductID.'</productID><operCode>START SSD</operCode><isAutoExtend>0</isAutoExtend><channelID>4</channelID><extensionInfo><namedParameters><key>SubType</key><value>0</value></namedParameters></extensionInfo></subInfo></loc:unSubscribeProductReq></loc:unSubscribeProductRequest></soapenv:Body></soapenv:Envelope>';

$params = array('http' => array(
              		  'method' => 'POST',
              		  'content' => $strXML
            		));

$requestedXML	= stream_context_create($params);

 

  	  
$response = @file_get_contents("http://10.16.100.194:8310/SubscribeManageService/services/SubscribeManage", false, $requestedXML);


date_default_timezone_set("Asia/Dhaka");

// write log Start and previous file name: subs_ROBI_URL_Call_request.tx

$fileName ="Un_subscription_req_res_".(string)date("Y_m_d",time()).".txt";
$step   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
$file = fopen("logs/".$fileName,'a+');

fwrite($file, $step.date("Y-m-d H:i:s",time()).PHP_EOL."-------------------".PHP_EOL.$strXML.$step);

fwrite($file, $step.date("Y-m-d H:i:s",time())." | ".$msisdn.PHP_EOL."-------------------".PHP_EOL.$response.$step);
fclose($file);

$p = xml_parser_create();
xml_parse_into_struct($p, $response, $vals, $index);
xml_parser_free($p);

$result = $vals[4]['value'];


if($result == "00000000")
{
  echo "1";
}
elseif($result == "22007008")
{
  echo "2";
}
else
{
  echo "0";
}

?>
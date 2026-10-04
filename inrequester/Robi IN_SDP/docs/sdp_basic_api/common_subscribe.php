<?php
require_once 'sdp_services_config.php';
include_once 'service_config.php';

$time  = "1408959098";
 $spPassword = "Robi1234";
 $timestamp = "1408959098";
 
 $package_id = trim($_REQUEST['package_id']);
 $shortcode=$_REQUEST['bno'];
 
$msisdn = trim($_REQUEST['msisdn']);

$msisdn=substr($msisdn,-10);
$code=substr($msisdn,0,2);
$keypress= trim($_REQUEST['keypress']);

$Ano=$msisdn;

if($code !='18' and $code !='16')
{
  echo 0;

  exit;

}



if ($keypress==1 || $keypress=='1'){

   if($package_id<>"") {
      $shortcode_service="shortCode"."_".$shortcode."_".$package_id;
   }
   else{
      $shortcode_service="shortCode_".$shortcode;
   }
}
else if($keypress==2 || $keypress=='2'){

   if($package_id<>"") {
      $shortcode_service="shortCode"."_".$shortcode."_".$package_id."_nonAuto";
   }
   else {
      $shortcode_service="shortCode_".$shortcode."_nonAuto";
   }
}
else {

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

$strXML 	= '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:loc="http://www.csapi.org/schema/parlayx/subscribe/manage/v1_0/local"><soapenv:Header><tns:RequestSOAPHeader xmlns:tns="http://www.huawei.com.cn/schema/common/v2_1"><tns:spId>'.$spId.'</tns:spId>
<tns:spPassword>'.$pass.'</tns:spPassword><tns:timeStamp>'.$time.'</tns:timeStamp></tns:RequestSOAPHeader></soapenv:Header><soapenv:Body>
<loc:subscribeProductRequest><loc:subscribeProductReq><userID><ID>'.$msisdn.'</ID><type>0</type></userID><subInfo><productID>'.$ProductID.'</productID><operCode>START IAP</operCode><isAutoExtend>0</isAutoExtend><channelID>4</channelID></subInfo></loc:subscribeProductReq></loc:subscribeProductRequest></soapenv:Body>
</soapenv:Envelope>';


 

if( !isset($msisdn) || $msisdn == "" ){

$fileName ="Subscription_req_res_".(string)date("Y_m_d",time()).".txt";
$step   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
$file = fopen("logs/".$fileName,'a+');
fwrite($file, $step.date("Y-m-d H:i:s",time())." | ".$msisdn.PHP_EOL."-------------------".PHP_EOL."Not Requested as msisdn is NULL".$step);
fclose($file);
exit();
}

if( !isset($ProductID) || $ProductID == "" ){

$fileName ="Subscription_req_res_".(string)date("Y_m_d",time()).".txt";
$step   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
$file = fopen("logs/".$fileName,'a+');
fwrite($file, $step.date("Y-m-d H:i:s",time())." | ".$msisdn.PHP_EOL."-------------------".PHP_EOL."Not Requested as ProductID is NULL".$step);
fclose($file);
exit();
}

$params = array('http' => array(
              		  'method' => 'POST',
              		  'content' => $strXML
            		));

$requestedXML	= stream_context_create($params);

  	  
$response = @file_get_contents("http://10.16.100.194:8310/SubscribeManageService/services/SubscribeManage", false, $requestedXML);

date_default_timezone_set("Asia/Dhaka");  

 

$fileName ="Subscription_req_res_".(string)date("Y_m_d",time()).".txt";
$step   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
$file = fopen("logs/".$fileName,'a+');

fwrite($file, $step.date("Y-m-d H:i:s",time()).PHP_EOL."-------------------".PHP_EOL."package_id: ".$package_id.PHP_EOL.$strXML.$step);

fwrite($file, $step.date("Y-m-d H:i:s",time())." | ".$msisdn.PHP_EOL."-------------------".PHP_EOL.$response.$step);
fclose($file);

$p = xml_parser_create();
xml_parse_into_struct($p, $response, $vals, $index);
xml_parser_free($p);


$result = $vals[4]['value'];


$servicename = urlencode($servicename);
$package_id = urlencode($package_id);


if($result == "00000000")
{
  $cdrWriteUrl = "http://10.101.60.8/sdp/cdrwriter.php?ServiceID=$servicename&Ano=$Ano&ResultCode=$result&Amount=$Amount&Purpose=Subscription&Bno=$shortcode_name&package_id=$package_id";
		file_get_contents($cdrWriteUrl);  
  echo "1";
}
elseif($result == "22007201"  )  //  22007201 already registered  .
{
 
	
   $cdrWriteUrl = "http://10.101.60.8/sdp/cdrwriter.php?ServiceID=$servicename&Ano=$Ano&ResultCode=$result&Amount=0&Purpose=Subscription&Bno=$shortcode_name&package_id=$package_id";
		file_get_contents($cdrWriteUrl);   
	
    echo "2";
}

elseif( $result == "22007330")  //   22007330 The account balance is insufficient.
{
	
   
   $cdrWriteUrl = "http://10.101.60.8/sdp/cdrwriter.php?ServiceID=$servicename&Ano=$Ano&ResultCode=$result&Amount=0&Purpose=Subscription&Bno=$shortcode_name&package_id=$package_id";
		file_get_contents($cdrWriteUrl);  

	 
	
    echo "1";
}
else
{
     $cdrWriteUrl = "http://10.101.60.8/sdp/cdrwriter.php?ServiceID=$servicename&Ano=$Ano&ResultCode=$result&Amount=0&Purpose=Subscription&Bno=$shortcode_name&package_id=$package_id";
		file_get_contents($cdrWriteUrl);  
    
  echo "0";
}

?>
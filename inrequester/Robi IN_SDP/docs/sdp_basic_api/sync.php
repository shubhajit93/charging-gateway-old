<?php
//header('Content-Type: text/xml');
$request_xml = file_get_contents("php://input");
include_once 'config.php';
include_once 'service_config.php';
require_once 'services_configuration.php';

$cn=connectDB(); 
//echo '1'; exit;
$p = xml_parser_create();
xml_parse_into_struct($p, $request_xml, $vals, $index);
xml_parser_free($p);


$toStatus  = $vals[11]['value'];
$msisdn    = substr($vals[4]['value'], -10);
$ProductID = $vals[8]['value'];
$ServiceID = $vals[9]['value'];


date_default_timezone_set("Asia/Dhaka");

// write sync log Start previous file name: trace_Subscription.txt

$fileName ="Sync_".(string)date("Y_m_d_A",time()).".txt";
$step   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
$file = fopen("logs/".$fileName,'a+');
fwrite($file, $step.date("Y-m-d H:i:s",time()).PHP_EOL."-------------------".PHP_EOL.$request_xml.$step);
fclose($file);
 


$product_service="product_$ProductID";

 

 if(array_key_exists($product_service, $service_info))
{
	   $spId 		= $service_info[$product_service]['spId'];		
	   $shortcode_name	= $service_info[$product_service]['shortcode'];
	   $subscriptionroot=$service_info[$product_service]['subscriptionroot'];
	   $subscriptiongroupid=$service_info[$product_service]['subscriptiongroupid'];

}

if($toStatus=='1')
{ 
	$cgwurl= $SyncOrderURL[$ServiceID]['reg_url']."?msisdn=$msisdn&msg=REG&ProductID=$ProductID";
	$result=file_get_contents($cgwurl);
		 
}//end of if for Registration
else if($toStatus=='2') 
{
	$url=$SyncOrderURL[$ServiceID]['dereg_url']."?msisdn=$msisdn&msg=DEREG&ProductID=$ProductID";
	$result=file_get_contents($url);        		
}
  
/* inserting sdp logs start */	
ClosedDBConnection($cn);

if($sdp_logs_enable==1)
{
SetDBInfo($Server_sdp_logs,$Database_sdp_logs,$UserID_sdp_logs,$Password_sdp_logs,$dbtype_sdp_logs);
$cn=connectDB();
$qry="insert into logs (`toStatus`,`msisdn`,`shortcode`,`productid`,`serviceid`,`lastupdate`) values ('$toStatus','$msisdn','$shortcode_name','$ProductID','$ServiceID',NOW())";
Sql_exec($cn,$qry);


 
 	
ClosedDBConnection($cn);	
}

/* inserting sdp logs end */	

$result = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:loc="http://www.csapi.org/schema/parlayx/data/sync/v1_0/local"><soapenv:Header/><soapenv:Body><loc:syncOrderRelationResponse><loc:result>0</loc:result><loc:resultDescription>OK</loc:resultDescription></loc:syncOrderRelationResponse></soapenv:Body></soapenv:Envelope>';
echo $result;

/* reset existing database connection */

function SetDBInfo($dbservers, $dbnames, $dbusername, $dbpassword, $dbtypes) {
    global $Server;
    global $Database;
    global $UserID;
    global $Password;
    global $dbtype;

    $dbtype = $dbtypes;
    $Server = $dbservers;
    $Database = $dbnames;
    $UserID = $dbusername;
    $Password = $dbpassword;
}

if($toStatus=='3') //for renew && $shortcode_name=='2828'
{
	
	date_default_timezone_set("Asia/Dhaka");

// write sync log Start previous file name: trace_Subscription.txt

$fileName ="Sync_renew_".(string)date("Y_m_d_A",time()).".txt";
$step   = PHP_EOL."------------------------------------------------------------------------------------------------------------".PHP_EOL;
$file = fopen("logs/".$fileName,'a+');
fwrite($file, $step.date("Y-m-d H:i:s",time()).PHP_EOL."-------------------".PHP_EOL.$shortcode_name."|".$msisdn.$step);
fclose($file);

    $cgwurl= $SyncOrderURL[$ServiceID]['reg_url']."?msisdn=$msisdn&msg=REG&ProductID=$ProductID";
    $result=file_get_contents($cgwurl);
}
?>
/**
 * This function is called by the notification system to handle the received
 * notification.
 * 
 * @param message
 *            The received notification (XML).
 */
function myUpdateParameters(message) {
	// only process notification of PARAMETER type
	var pArray = message.getElementsByTagName('PARAMETER');

	if (pArray != null) {
		// loop through the array of parameters contained in this notification
		for (var i = 0; i < pArray.length; i++) {
			if (pArray[i] != null) {
				pNameNode = pArray[i].getElementsByTagName('NAME')[0];
				pValueNode = pArray[i].getElementsByTagName('VALUE')[0];
				if (pNameNode != null && pValueNode != null) {
					// get the parameter name and its value
					pName = pNameNode.childNodes[0].nodeValue;
					pValue = pValueNode.textContent;

					console.debug(pName);
					console.debug(pValue);

					// decode and handle JSON encoded parameters
					if (pName.indexOf('JSON_') == 0) {
						pDecoded = JSON.parse(pValue);
						realName = pName.substring(5);

						console.log(realName);
						console.debug(pDecoded);
					}
					/*
					 * handle regular parameters by name
					 * 
					 * again, different methods of updating the document are
					 * used (regular JS and jQuery)
					 */
					else {
						if (pName === 'STRING_PARAMETER1') {
							$('#stringParameter1').val(pValue);
						} else if (pName === 'INTEGER_PARAMETER') {
							document.getElementById('integerParameter').value = pValue;
						} else if (pName === 'BOOLEAN_PARAMETER') {
							$('#booleanParameter').val(pValue);
						}
					}
				}
			}
		}
	}
}
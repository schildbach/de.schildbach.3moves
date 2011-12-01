var common = new Object();

common.Refresh = function(id, url, interval)
{
	this.id = id;
	this.url = url;
	this.interval = interval;
	this.set();
}

common.Refresh.prototype =
{
	set: function()
	{
		var oThis = this;
		setTimeout(function() { oThis.timeout() }, this.interval);
	},

	timeout: function()
	{
		var oThis = this;
		
		var handleSuccess = function(request)
		{
			if(request.responseXML !== undefined)
			{
				var elm = request.responseXML.documentElement;
				var newNode = oThis.importNode(document, elm);
				node = document.getElementById(oThis.id);
				if(node.firstChild != null)
					node.replaceChild(newNode, node.firstChild);
				else
					node.appendChild(newNode);
				oThis.set();
			} 
		} 
	
		var handleFailure = function(request)
		{
		} 
	
		var callback = 
		{ 
			success: handleSuccess,
			failure: handleFailure,
			timeout: 2000
		};
	
		YAHOO.util.Connect.asyncRequest('GET', this.url, callback);
	},

	importNode: function(domDocument, node)
	{
		switch (node.nodeType)
		{
			case 1:
				var element = domDocument.createElement(node.nodeName);
				for (var i = 0; i < node.attributes.length; i++)
				{
					if (node.attributes[i].nodeName == 'class')
					{
						element.className = node.attributes[i].nodeValue;
					}
					else
					{
						element.setAttribute(node.attributes[i].nodeName, node.attributes[i].nodeValue);
					}
				}
				for (var i = 0; i < node.childNodes.length; i++)
					element.appendChild(this.importNode(domDocument, node.childNodes[i]));
				return element;
				break;
			case 3:
				var textNode = domDocument.createTextNode(node.nodeValue);
				return textNode;
				break;
			/* add other nodes here */
		}
	}
};

function serverMessage(serverPath, inputFieldId, messageElementId)
{
	var handleSuccess = function(o)
	{
		if(o.responseText !== undefined)
		{
			document.getElementById(messageElementId).innerHTML = o.responseText; 
		}
	}

	var handleFailure = function(o)
	{
		document.getElementById(messageElementId).innerHTML = '';
	}

	var callback = 
	{
		success: handleSuccess,
		failure: handleFailure,
		timeout: 2000
	};

	YAHOO.util.Connect.asyncRequest('GET', serverPath + document.getElementById(inputFieldId).value, callback);
}

function initTooltips(peopleUrl, gameUrl, loadingUrl)
{
	Ext.Updater.defaults.indicatorText = '<img style="display:block" src="' + loadingUrl + '"/>';

	Ext.select('a.usertooltip').each(function(el, th, index)
	{
		var tip = new Ext.ToolTip(
		{
			target: el,
			autoWidth: true,
			autoHeight: true,
			shadow: false,
			frame: false,
			trackMouse: true,
			cls: 'tooltip',
			autoLoad:
			{
				url: peopleUrl.replace(/\%s/, el.dom.name)
			}
	    });
	});

	Ext.select('a.gametooltip').each(function(el, th, index)
	{
		var tip = new Ext.ToolTip(
		{
			target: el,
			autoWidth: true,
			autoHeight: true,
			shadow: false,
			frame: false,
			trackMouse: true,
			cls: 'tooltip',
			autoLoad:
			{
				url: gameUrl.replace(/\%s/, el.dom.name)
			}
	    });
	});
}

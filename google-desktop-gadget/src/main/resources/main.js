﻿/*
Copyright (C) 2009 Jeff Black

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/ 

/**
 * How often to check for new job status, if we're offline
 * @type Number
 */
var CHECK_ONLINE_STATUS_INTERVAL_MS = 30 * 1000;  // 30 seconds

/**
 * Keeps track of the current updateStatus timer. Used to ensure that
 * we don't have multiple updateStatus timers at the same time.
 */
var timerToken = null;

/**
 * Default to a local Hudson instance on port 8080
 */
var DEFAULT_HUDSON_URL = "";

/**
 * Default to a polling every 5 minutes
 */
var DEFAULT_POLLING_INTERVAL_MINUTES = 1;
var pollingIntervalMinutes = DEFAULT_POLLING_INTERVAL_MINUTES;

var hudsonViewUrls = "";			    // the urls as a single string
var hudsonViewList = new Array();	// array of urls
var hudsonViewData = new Array();	// hash of views and jobs

var animatedJobStatusElements = new Array();

function view_onOpen() {
    initializeStoredOptions();

	if (populateViewDataFromOptions() > 0) {
		updateStatus();
	}
}

function initializeStoredOptions() {
	options.putDefaultValue('hudsonUrlsProp', "");
	options.putDefaultValue('intervalMinutesProp', DEFAULT_POLLING_INTERVAL_MINUTES);
}

/**
 * Populate the views we need to poll from options
 */
function populateViewDataFromOptions() {
    // load options from database
	pollingIntervalMinutes = options.getValue("intervalMinutesProp");
    hudsonViewUrls = options.getValue("hudsonUrlsProp");
	
	// urls are stored comma separated
	hudsonViewList = hudsonViewUrls.split(",");
	hudsonViewData = [];
	for (viewUrlIndex in hudsonViewList) {
		var viewUrl = hudsonViewList[viewUrlIndex];
		if (viewUrl.length > 0) {
			// var hudsonView = new HudsonView(viewUrl, new MockPolling(renderView));
			var hudsonView = new HudsonView(viewUrl, new NetworkPolling(renderView));
			hudsonViewData.push(hudsonView);
		}
	}

	return hudsonViewData.length;
}

/**
 * Force a refresh if hudson url or polling interval changes
 */
function onOptionChanged() {

	// stop any current timer
	if (timerToken) {
		view.clearTimeout(timerToken);
		timerToken = null;
	}

	if (populateViewDataFromOptions() > 0) {
		updateStatus();
	}
}

/**
 * delete all previous jobs and statuses and recreate with latest jobs and statuses
 */ 
function renderView(updatedHudsonView) {

	if (hudsonViewData.length >= 1) {

		contentListbox.removeAllElements();
		animatedJobStatusElements = [];

		// if view is defined, set the new job status info for view in view hash
		if (updatedHudsonView) {
			for (viewIndex in hudsonViewData) {
				var existingView = hudsonViewData[viewIndex];
				if (updatedHudsonView.url == existingView.url) {
					hudsonViewData[viewIndex] = updatedHudsonView;
					break;
				}
			}
		}

		var listboxY = 0;
		var imgX = view.width - 26;
		var listboxWidth = imgX - 5;

		// add the view as a list element header in the listbox
		for (viewIndex in hudsonViewData) {

			var viewToRender = hudsonViewData[viewIndex];

			var viewExpander = "<button name='expanderImg' width='16' height='16' x='0' y='2' image='images/document_add.gif' onclick='toggleViewCollapse(" + viewToRender.id + ")'/>";
			var viewLink = "<a width='" + listboxWidth + "' height='16' x='20' href='" + viewToRender.url + "'>" + viewToRender.url + "</a>";
			var viewImg = "<img width='16' height='16' x='" + imgX + "' y='2' tooltip='" + viewToRender.getNetworkStatus() + "' src='images/" + viewToRender.getColor() + ".gif'/>";
			var header = contentListbox.appendElement("<item background='#DDDDDD'>" + viewExpander + viewLink + viewImg + "</item>");

			if (viewToRender.expanded) {

				header.children("expanderImg").image="images/document_delete.gif";
				var jobs = viewToRender.getJobs();

				// each job in the view is rendered as an item under the view element in the listbox
				for (jobIndex in jobs) {
					var job = jobs[jobIndex];
					var jobLink = "<a width='" + listboxWidth + "' height='16' x='10' href='" + job.url + "'>" + job.name + "</a>";
					var jobImg = "<img name='jobImg' width='16' height='16' x='" + imgX + "' y='2' src='images/" + job.color + ".gif'/>";
					var jobElement = contentListbox.appendElement("<item valign='center'>" + jobLink + jobImg + "</item>");

					if (job.color.substr(job.color.length-6) == "_anime") {
						animatedJobStatusElements.push(jobElement.children("jobImg"));
					}
				}
				
			}
		}

		if (animatedJobStatusElements.length > 0) {
			animateFadeIn();
		}
	}	
	
	sb_onchange();

}

function getViewById(id) {
	for (viewIndex in hudsonViewData) {
		var existingView = hudsonViewData[viewIndex];
		if (id == existingView.id) {
			return existingView;
		}
	}
}

function getViewCount() {
	var count = 0;
	for (viewIndex in hudsonViewData) {
		count += 1;
		var existingView = hudsonViewData[viewIndex];
		count += existingView.getJobs().length;
	}
	return count;
}

function toggleViewCollapse(viewId) {
	var hudsonView = getViewById(viewId);
	hudsonView.toogleExpanded();
	renderView(hudsonView);
}

function updateStatus() {
	setLastUpdatedTime();
    if (hudsonViewData.length >= 1) {
		for (viewIndex in hudsonViewData) {
			var viewToUpdate = hudsonViewData[viewIndex];
			viewToUpdate.updateViewStatus();
		}
		debug.trace('polling complete...');
	}

	// make sure updateStatus gets called again
	registerUpdateStatus();
}

/**
 * Sets a timeout for updateStatus to be called depending on the
 * online/offline state
 */
function registerUpdateStatus() {
	var timeout;
	if (framework.system.network.online) {
		timeout = pollingIntervalMinutes * 60 * 1000;
	} else {
		timeout = CHECK_ONLINE_STATUS_INTERVAL_MS;
	}

	if (timerToken) {
		view.clearTimeout(timerToken);
		timerToken = null;
	}

	timerToken = view.setTimeout(updateStatus, timeout);
}

function setLastUpdatedTime() {
	var currentTime = new Date();
	var hours = currentTime.getHours();
	var minutes = currentTime.getMinutes();
	if (hours < 10) {
		hours = "0" + hours;
	}
	if (minutes < 10) {
		minutes = "0" + minutes;
	}
	lastPollTime.value = hours + ":" + minutes;
}

function onOpenOptionsClick() {
    pluginHelper.ShowOptionsDialog();
} 

function sb_onchange() {
	var viewCount = getViewCount();

	contentListbox.height = Math.max(viewCount * contentListbox.itemHeight, contentDiv.height);
	contentScrollbar.max = contentListbox.height - contentDiv.height;
	if (contentListbox.height > (viewCount * contentListbox.itemHeight) ) {
		contentScrollbar.visible = false;
	} else {
		contentScrollbar.visible = true;
	}
	contentListbox.y = Math.min(0, -contentScrollbar.value)  
}

function view_onSize() {
	//Minimum SIze
	//120x60

	main.height = Math.max(view.height, 60);
	main.width = Math.max(view.width, 120);
	contentDiv.height = Math.max(main.height - 20, 20);
	contentDiv.width =  Math.max(main.width, 120);

	contentListbox.width = contentDiv.width - 10
	contentListbox.itemWidth = contentListbox.width;

	forceRefresh.x = main.width - 25;

	contentScrollbar.height = contentDiv.height;
	contentScrollbar.width = 10
	contentScrollbar.x = contentDiv.width - 10;
	contentScrollbar.max = Math.max(contentDiv.height);
	contentScrollbar.value = 0;
	sb_onchange();
	renderView();
}

// TODO:remove these functions when google desktop supports animated gifs

var anim;

function animateFadeIn() {
		anim = beginAnimation("fadeIn()", 0, 255, 500);
}

function animateFadeOut() {
		anim = beginAnimation("fadeOut()", 255, 0, 500);
}

function fadeIn() {
	for (eIndex in animatedJobStatusElements) {
		animatedJobStatusElements[eIndex].opacity = event.value;
	}
	if (event.value == 255) {
		cancelAnimation(anim);
		anim = null;
		setTimeout("animateFadeOut()", 500);
//		animateFadeOut();
	}
}

function fadeOut() {
	for (eIndex in animatedJobStatusElements) {
		animatedJobStatusElements[eIndex].opacity = event.value;
	}
	if (event.value == 0) {
		cancelAnimation(anim);
		anim = null;
		setTimeout("animateFadeIn()", 100);
//		animateFadeIn();
	}
}

// TODO:remove these functions when google desktop supports animated gifs
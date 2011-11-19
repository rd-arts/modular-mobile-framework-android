
/*
 This is application support API.
 These functions provide the call on the Module itself by JS Part
 */

var kAuthenticationToken = "kAuthenticationToken";    

var RCApplicationModuleDelegate = {

    showAlert: function(message) {
        window.location.href = "rpa://showAlert?message="+escape(message);
    },

    setModuleData: function(data,identifier) {
        window.location.href = "rpa://setModuleData?data="+data+"&identifier="+identifier;
    },

    incrementApplicationIconBadgeNumberBy: function(amount) {
        window.location.href = "rpa://incrementApplicationIconBadgeNumberBy?amount="+amount;    
    },

    decrementApplicationIconBadgeNumberBy: function(amount) {
        window.location.href = "rpa://decrementApplicationIconBadgeNumberBy?amount="+amount;    
    },

    setUIBadgeValue: function(badgeValue) {
        window.location.href = "rpa://setUIBadgeValue?badgeValue="+badgeValue;    
    },

    relinquishUIFocus: function() {
        window.location.href = "rpa://relinquishUIFocus"; 
    },

    relinquishUIFocusToModule: function(moduleIdentifier) {
        window.location.href = "rpa://relinquishUIFocusToModule?moduleIdentifier="+moduleIdentifier;
    }
};

package ng.gov.eirs.mas.erasmpoa.data.constant


interface SubmissionType {
    companion object {
        const val OFFLINE = "offline"
        const val ONLINE = "online"
    }
}

interface SettlementMethod {
    companion object {
        const val SCRATCH_CARD = 3
    }
}

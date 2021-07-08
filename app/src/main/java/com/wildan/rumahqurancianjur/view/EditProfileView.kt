package com.wildan.rumahqurancianjur.view

class EditProfileView {

    interface View {
        fun onSuccessSaveData(message: String)
        fun showProfileUser(
            name: String?,
            email: String?,
            phone: String?,
            address: String?,
            gender: String?
        )

        fun handleResponse(message: String)
        fun hideProgressBar()
        fun showProgressBar()
        fun hideProgressButton()
        fun showProgressButton()
    }

    interface Presenter {
        fun showDataUser()
        fun requestEditProfile(newEmail: String, newUser: HashMap<String, String>)
    }
}
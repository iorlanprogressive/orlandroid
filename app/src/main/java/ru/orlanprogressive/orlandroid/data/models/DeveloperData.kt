package ru.orlanprogressive.orlandroid.data.models

data class DeveloperData(
	val developer: Developer,
	val websites: List<Contact>,
	val emails: List<Contact>,
	val phones: List<Contact>,
)

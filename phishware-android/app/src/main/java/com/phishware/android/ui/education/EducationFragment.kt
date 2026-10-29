package com.phishware.android.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentEducationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EducationFragment : Fragment() {
    private var _binding: FragmentEducationBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var repository: PhishwareRepository

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentEducationBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRefresh.setOnClickListener { load() }
        load()
    }

    private fun load() {
        binding.tvList.text = "Cargando lecciones…"
        lifecycleScope.launch {
            repository.getEducation(0, 20).onSuccess { page ->
                if (page.content.isEmpty()) { binding.tvList.text = "Sin contenido todavía."; return@launch }
                binding.tvList.text = page.content.joinToString("\n\n") { "📘 ${it.title}\n${it.summary.orEmpty()}\n[${it.slug}]" }
                // Tap largo simulado: primer slug con quiz
                binding.btnQuiz.setOnClickListener { openQuiz(page.content.firstOrNull()?.slug.orEmpty()) }
                binding.btnDetail.setOnClickListener { openDetail(page.content.firstOrNull()?.slug.orEmpty()) }
            }.onFailure { binding.tvList.text = "Error: ${it.message}" }
        }
    }

    private fun openDetail(slug: String) {
        if (slug.isBlank()) return
        lifecycleScope.launch {
            repository.getEducationDetail(slug).onSuccess {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(it.title)
                    .setMessage((it.summary.orEmpty() + "\n\n" + it.content.orEmpty()).take(1500))
                    .setPositiveButton("Cerrar", null)
                    .show()
            }.onFailure { Snackbar.make(binding.root, it.message ?: "Error", Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun openQuiz(slug: String) {
        if (slug.isBlank()) return
        lifecycleScope.launch {
            repository.getQuizByModule(slug).onSuccess { quiz ->
                val qs = quiz.questions.orEmpty().sortedBy { it.orderIndex }
                if (qs.isEmpty()) { Snackbar.make(binding.root, "Este módulo aún no tiene quiz", Snackbar.LENGTH_LONG).show(); return@launch }
                // Quiz simple: responde solo la primera pregunta (demo móvil); resto en web
                val q = qs.first()
                val opts = q.options?.toSortedMap() ?: emptyMap()
                val items = opts.map { "${it.key}: ${it.value}" }.toTypedArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(q.questionText)
                    .setItems(items) { _, which ->
                        val selectedKey = opts.keys.sorted()[which]
                        lifecycleScope.launch {
                            repository.submitQuiz(quiz.id, mapOf(q.id.toString() to selectedKey))
                                .onSuccess { r -> Snackbar.make(binding.root, "Puntaje: ${r.score} ${if (r.passed) "✅" else "❌"} (+${r.pointsEarned} pts)", Snackbar.LENGTH_LONG).show() }
                                .onFailure { Snackbar.make(binding.root, it.message ?: "Error", Snackbar.LENGTH_LONG).show() }
                        }
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }.onFailure { Snackbar.make(binding.root, it.message ?: "Sin quiz", Snackbar.LENGTH_LONG).show() }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

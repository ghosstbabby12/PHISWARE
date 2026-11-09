package com.phishware.android.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.phishware.android.R
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

    private val featuredAdapter = EducationFeaturedAdapter { openDetail(it.slug) }
    private val allAdapter = EducationAdapter(
        onRead = { openDetail(it.slug) },
        onQuiz = { openQuiz(it.slug) }
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentEducationBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFeatured.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeatured.adapter = featuredAdapter
        binding.rvAll.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAll.adapter = allAdapter
        binding.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            repository.getEducation(0, 20).onSuccess { page ->
                binding.swipeRefresh.isRefreshing = false
                if (page.content.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = getString(R.string.edu_empty)
                    binding.rvFeatured.visibility = View.GONE
                    binding.rvAll.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvFeatured.visibility = View.VISIBLE
                    binding.rvAll.visibility = View.VISIBLE
                    featuredAdapter.submitList(page.content.take(5))
                    allAdapter.submitList(page.content)
                }
            }.onFailure {
                binding.swipeRefresh.isRefreshing = false
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text = getString(R.string.edu_error_fmt, it.message ?: "")
            }
        }
    }

    private fun openDetail(slug: String) {
        if (slug.isBlank()) return
        lifecycleScope.launch {
            repository.getEducationDetail(slug).onSuccess {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(it.title)
                    .setMessage((it.summary.orEmpty() + "\n\n" + it.content.orEmpty()).take(1500))
                    .setPositiveButton(getString(R.string.common_close), null)
                    .show()
            }.onFailure { Snackbar.make(binding.root, it.message ?: getString(R.string.common_error), Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun openQuiz(slug: String) {
        if (slug.isBlank()) return
        lifecycleScope.launch {
            repository.getQuizByModule(slug).onSuccess { quiz ->
                val qs = quiz.questions.orEmpty().sortedBy { it.orderIndex }
                if (qs.isEmpty()) { Snackbar.make(binding.root, getString(R.string.edu_no_quiz), Snackbar.LENGTH_LONG).show(); return@launch }
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
                                .onSuccess { r -> Snackbar.make(binding.root, getString(R.string.edu_quiz_score_fmt, r.score, if (r.passed) getString(R.string.edu_quiz_passed) else getString(R.string.edu_quiz_failed), r.pointsEarned), Snackbar.LENGTH_LONG).show() }
                                .onFailure { Snackbar.make(binding.root, it.message ?: getString(R.string.common_error), Snackbar.LENGTH_LONG).show() }
                        }
                    }
                    .setNegativeButton(getString(R.string.common_close), null)
                    .show()
            }.onFailure { Snackbar.make(binding.root, it.message ?: getString(R.string.edu_no_quiz2), Snackbar.LENGTH_LONG).show() }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
